package ru.practicum.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventFilterParams;
import ru.practicum.dto.event.EventFilterParamsDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.event.UpdateEventRequest;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enums.EventSort;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.enums.StateActionAdmin;
import ru.practicum.enums.StateActionUser;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.Category;
import ru.practicum.model.ConfirmedRequest;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.User;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.AdminEventService;
import ru.practicum.service.PrivateEventService;
import ru.practicum.service.PublicEventService;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.util.DateTimeFormatter.mapStringToLocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventServiceImpl implements AdminEventService, PublicEventService, PrivateEventService {
    private final EventRepository eventRepo;
    private final UserRepository userRepo;
    private final ParticipationRequestRepository requestRepo;
    private final CategoryRepository categoryRepo;
    private final LocationRepository locationRepo;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final ParticipationRequestMapper participationRequestMapper;
    private final StatsService statsService;

    @Override
    @Transactional
    public EventFullDto createEventByPrivate(NewEventDto newEventDto, Long userId) {
        Event event = eventMapper.mapToEvent(newEventDto);
        User user = checkUserExistAndGet(userId);
        Category category = checkCategoryExistAndGet(newEventDto.getCategory());
        Location location = getLocation(newEventDto.getLocation());
        event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(location);
        event.setState(EventState.PENDING);
        checkDateTimeIsAfterNowWithGap(event.getEventDate(), 2);
        Event savedEvent = eventRepo.save(event);
        log.info("Событие " + savedEvent + "сохранено.");
        return eventMapper.mapToEventFullDto(savedEvent);
    }

    @Override
    public List<EventShortDto> getEventsByPrivate(Long userId, Integer from, Integer size) {
        checkUserExistAndGet(userId);
        int page = from / size;
        List<Event> events = eventRepo.findByInitiatorId(userId, PageRequest.of(page, size));
        statsService.getViewsList(events);
        getConfirmedRequests(events);
        return new ArrayList<>(eventMapper.mapToEventShortDtoListForEvents(events));
    }

    @Override
    public EventFullDto getEventByPrivate(Long userId, Long eventId) {
        checkUserExistAndGet(userId);
        Event event = checkEventExistAndGet(eventId);
        getConfirmedRequests(List.of(event));
        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByPrivate(UpdateEventUserRequest request, Long userId, Long eventId) {
        checkUserExistAndGet(userId);
        Event event = checkEventExistAndGet(eventId);
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Невозможно обновить опубликованное событие.");
        }
        updateEventFields(event, request);
        updateEventStateAction(event, request.getStateAction());
        eventRepo.save(event);
        log.info("Событие " + event + "обновлено.");
        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByPrivate(Long userId, Long eventId) {
        checkUserExistAndGet(userId);
        return requestRepo.findByEventId(eventId)
                .stream()
                .map(participationRequestMapper::mapToRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateByPrivate(EventRequestStatusUpdateRequest update, Long userId, Long eventId) {
        checkUserExistAndGet(userId);
        Event event = checkEventExistAndGet(eventId);
        List<Long> requestIds = update.getRequestIds();
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        if (!isRequestStatusUpdateAllowed(event, update)) {
            return result;
        }
        List<ParticipationRequest> requestsToUpdate = requestRepo.findAllByIdIn(requestIds);
        checkAllRequestsPending(requestsToUpdate);
        RequestStatus status = RequestStatus.valueOf(update.getStatus());
        if (status == RequestStatus.CONFIRMED) {
            confirmAndSetInResult(requestsToUpdate, result, event);
        } else if (status == RequestStatus.REJECTED) {
            rejectAndSetInResult(requestsToUpdate, result);
        }
        return result;
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(EventFilterParamsDto paramsDto) {
        EventFilterParams params = convertInputParams(paramsDto);
        List<Event> events = eventRepo.adminEventsSearch(params);
        statsService.getViewsList(events);
        getConfirmedRequests(events);
        return events.stream()
                .map(eventMapper::mapToEventFullDto)
                .sorted(getComparator(params.getSort()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(UpdateEventAdminRequest request, Long eventId) {
        Event event = checkEventExistAndGet(eventId);
        LocalDateTime actualDateTime = event.getEventDate();
        checkDateTimeIsAfterNowWithGap(actualDateTime, 1);
        LocalDateTime targetDateTime = request.getEventDate();
        if (Objects.nonNull(targetDateTime)) {
            checkDateTimeIsAfterNowWithGap(targetDateTime, 2);
        }
        StateActionAdmin action = request.getStateAction();
        if (Objects.nonNull(action)) {
            switch (action) {
                case PUBLISH_EVENT:
                    publishEvent(request, event);
                    break;
                case REJECT_EVENT:
                    rejectEvent(event);
                    break;
            }
        }
        return eventMapper.mapToEventFullDto(eventRepo.save(event));
    }

    @Override
    public EventFullDto getEventByPublic(Long eventId, HttpServletRequest request) {
        Event event = checkEventExistAndGet(eventId);
        boolean published = (event.getState() == EventState.PUBLISHED);
        if (!published) {
            throw new NotFoundException("Событие с id = " + eventId + " не опубликовано.");
        }
        EventFullDto eventFullDto = completeEventFullDto(event);
        statsService.addHit(request);
        return eventFullDto;
    }

    @Override
    public List<EventShortDto> getEventsByPublic(EventFilterParamsDto paramsDto, HttpServletRequest request) {
        EventFilterParams params = convertInputParams(paramsDto);
        List<Event> events = eventRepo.publicEventsSearch(params);
        statsService.getViewsList(events);
        getConfirmedRequests(events);
        statsService.addHit(request);
        return events.stream().map(eventMapper::mapToEventShortDto)
                .sorted(getComparator(params.getSort())).collect(Collectors.toList());
    }

    private void getConfirmedRequests(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        List<ConfirmedRequest> confirmedRequests = requestRepo.findConfirmedRequest(eventIds);
        Map<Long, Long> confirmedRequestsMap = confirmedRequests.stream()
                .collect(Collectors.toMap(ConfirmedRequest::getEventId, ConfirmedRequest::getCount));
        events.forEach(event -> event.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L)));
    }

    private Comparator<EventDto> getComparator(EventSort sortType) {
        return EventDto.getEventComparator(sortType);
    }

    private boolean isRequestStatusUpdateAllowed(Event event, EventRequestStatusUpdateRequest update) {
        return event.getRequestModeration() &&
                event.getParticipantLimit() > 0 &&
                !update.getRequestIds().isEmpty();
    }

    private void completeWithViews(EventDto eventDto) {
        Long eventId = eventDto.getId();
        Long views = statsService.getViews(eventId);
        eventDto.setViews(views);
    }

    private static void checkAllRequestsPending(List<ParticipationRequest> requests) {
        boolean allPending = requests.stream()
                .allMatch(r -> r.getStatus() == RequestStatus.PENDING);
        if (!allPending) {
            throw new ConflictException("Невозможно изменить статус запроса на участие.");
        }
    }

    private EventFilterParams convertInputParams(EventFilterParamsDto paramsDto) {
        EventFilterParams params;
        try {
            String startString = paramsDto.getRangeStart();
            String endString = paramsDto.getRangeEnd();
            LocalDateTime start = getFromStringOrSetDefault(startString, LocalDateTime.now());
            LocalDateTime end = getFromStringOrSetDefault(endString, null);
            if (end != null && end.isBefore(start)) {
                throw new BadRequestException("Неверные параметры фильтра временного диапазона.");
            }
            params = eventMapper.mapToEventFilterParams(paramsDto, start, end);
        } catch (UnsupportedEncodingException e) {
            throw new ConflictException("Неверные параметры поиска.");
        }
        return params;
    }

    private static LocalDateTime getFromStringOrSetDefault(String dateTimeString, LocalDateTime defaultValue) throws UnsupportedEncodingException {
        if (dateTimeString != null) {
            return mapStringToLocalDateTime(java.net.URLDecoder.decode(dateTimeString, StandardCharsets.UTF_8));
        }
        return defaultValue;
    }

    private void publishEvent(UpdateEventAdminRequest request, Event event) {
        EventState state = event.getState();
        if (state == EventState.PUBLISHED) {
            throw new ConflictException("Событие уже опубликовано.");
        }
        if (state == EventState.REJECTED) {
            throw new ConflictException("Событие уже отклонено.");
        }
        if (state == EventState.CANCELED) {
            throw new ConflictException("Событие уже отменено.");
        }
        updateEventFields(event, request);
        event.setState(EventState.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
    }

    private void rejectEvent(Event event) {
        EventState state = event.getState();
        if (state == EventState.PUBLISHED || state == EventState.CANCELED) {
            throw new ConflictException("Невозможно отклонить опубликованное событие.");
        }
        event.setState(EventState.CANCELED);
    }

    private void confirmAndSetInResult(List<ParticipationRequest> requestsToUpdate, EventRequestStatusUpdateResult result, Event event) {
        long confirmed = requestRepo.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        long limit = event.getParticipantLimit();

        for (ParticipationRequest request : requestsToUpdate) {
            if (confirmed == limit) {
                int start = requestsToUpdate.indexOf(request);
                int end = requestsToUpdate.size();
                rejectAndSetInResult(requestsToUpdate.subList(start, end), result);
                throw new ConflictException("Достигнут лимит участников.");
            }
            confirmAndSetInResult(List.of(request), result);
            confirmed++;
        }
    }

    private void rejectAndSetInResult(List<ParticipationRequest> requestsToUpdate, EventRequestStatusUpdateResult result) {
        setStatus(requestsToUpdate, RequestStatus.REJECTED);
        List<ParticipationRequest> rejectedRequests = requestRepo.saveAll(requestsToUpdate);
        result.setRejectedRequests(participationRequestMapper.mapToRequestDtoList(rejectedRequests));
    }

    private void confirmAndSetInResult(List<ParticipationRequest> requestsToUpdate, EventRequestStatusUpdateResult result) {
        setStatus(requestsToUpdate, RequestStatus.CONFIRMED);
        List<ParticipationRequest> confirmed = requestRepo.saveAll(requestsToUpdate);
        result.setConfirmedRequests(participationRequestMapper.mapToRequestDtoList(confirmed));
    }

    private void setStatus(List<ParticipationRequest> requestsToUpdate, RequestStatus status) {
        requestsToUpdate.forEach(r -> r.setStatus(status));
    }

    private User checkUserExistAndGet(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));
    }

    private Event checkEventExistAndGet(Long eventId) {
        return eventRepo.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не найдено."));
    }

    private Category checkCategoryExistAndGet(Long catId) {
        return categoryRepo.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id = " + catId + " не найдено."));
    }

    private Location getLocation(LocationDto locationDto) {
        Location location = locationMapper.mapToLocation(locationDto);
        return locationRepo.getByLatAndLon(location.getLat(), location.getLon())
                .orElse(locationRepo.save(location));
    }

    private EventFullDto completeEventFullDto(Event event) {
        EventFullDto eventFullDto = eventMapper.mapToEventFullDto(event);
        Long confirmedRequests = requestRepo.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        completeWithViews(eventFullDto);
        eventFullDto.setConfirmedRequests(confirmedRequests);
        return eventFullDto;
    }

    private void updateEventFields(Event event, UpdateEventRequest request) {
        updateEventAnnotation(event, request.getAnnotation());
        updateEventCategory(event, request.getCategory());
        updateEventDescription(event, request.getDescription());
        updateEventDate(event, request.getEventDate());
        updateEventLocation(event, request.getLocation());
        updateEventPaidStatus(event, request.getPaid());
        updateEventParticipationLimit(event, request.getParticipantLimit());
        updateEventRequestModeration(event, request.getRequestModeration());
        updateEventTitle(event, request.getTitle());
    }

    private void updateEventTitle(Event event, String title) {
        if (Objects.nonNull(title) && !title.isBlank()) {
            event.setTitle(title);
        }
    }

    private void updateEventStateAction(Event event, StateActionUser action) {
        if (Objects.nonNull(action)) {
            if (action == StateActionUser.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (action == StateActionUser.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }
    }

    private void updateEventRequestModeration(Event event, Boolean requestModeration) {
        if (Objects.nonNull(requestModeration)) {
            event.setRequestModeration(requestModeration);
        }
    }

    private void updateEventParticipationLimit(Event event, Long limit) {
        if (Objects.nonNull(limit)) {
            event.setParticipantLimit(limit);
        }
    }

    private void updateEventPaidStatus(Event event, Boolean paid) {
        if (Objects.nonNull(paid)) {
            event.setPaid(paid);
        }
    }

    private void updateEventLocation(Event event, LocationDto locationDto) {
        if (Objects.nonNull(locationDto)) {
            Location updatedLocation = getLocation(locationDto);
            event.setLocation(updatedLocation);
        }
    }

    private void updateEventDate(Event event, LocalDateTime eventDate) {
        if (Objects.nonNull(eventDate)) {
            checkDateTimeIsAfterNowWithGap(eventDate, 1);
            event.setEventDate(eventDate);
        }
    }

    private void updateEventDescription(Event event, String description) {
        if (Objects.nonNull(description) && !description.isBlank()) {
            event.setDescription(description);
        }
    }

    private void updateEventCategory(Event event, Long catId) {
        if (Objects.nonNull(catId)) {
            Category updated = checkCategoryExistAndGet(catId);
            event.setCategory(updated);
        }
    }

    private void updateEventAnnotation(Event event, String annotation) {
        if (Objects.nonNull(annotation) && !annotation.isBlank()) {
            event.setAnnotation(annotation);
        }
    }

    private void checkDateTimeIsAfterNowWithGap(LocalDateTime value, Integer gapFromNowInHours) {
        LocalDateTime minValidDateTime = LocalDateTime.now().plusHours(gapFromNowInHours);
        if (value.isBefore(minValidDateTime)) {
            throw new BadRequestException("Неверная дата и время события.");
        }
    }
}