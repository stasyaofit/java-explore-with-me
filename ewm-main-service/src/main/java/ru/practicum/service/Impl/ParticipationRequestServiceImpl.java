package ru.practicum.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.ParticipationRequestService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepo;
    private final EventRepository eventRepo;
    private final ParticipationRequestMapper mapper;


    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getAll(Long userId) {
        checkUserExistAndGet(userId);
        List<ParticipationRequest> participationRequests = requestRepository.findByRequesterId(userId);
        return mapper.mapToRequestDtoList(participationRequests);
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        if (requestRepository.findFirst1ByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new ConflictException("Заявка на участие уже существует.");
        }
        Event event = eventRepo.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id = " + eventId + " не найдено."));
        if (userId.equals(event.getInitiator().getId())) {
            throw new ConflictException("Организатор события не может создавать запросы на участие в собственном событии.");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Неверный статус события.");
        }
        if (event.getParticipantLimit() > 0) {
            Long participants = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
            Long limit = event.getParticipantLimit();
            if (participants >= limit) {
                throw new ConflictException("Лимит участников исчерпан.");
            }
        }
        ParticipationRequest request = completeNewParticipationRequest(userId, event);
        return mapper.mapToRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {
        checkUserExistAndGet(userId);
        ParticipationRequest request = requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрос на участие с id = " + requestId + " не найден."));
        request.setStatus(RequestStatus.CANCELED);
        return mapper.mapToRequestDto(requestRepository.save(request));
    }

    private ParticipationRequest completeNewParticipationRequest(Long userId, Event event) {
        User user = checkUserExistAndGet(userId);
        boolean needConfirmation = event.getRequestModeration();
        boolean hasParticipantsLimit = !event.getParticipantLimit().equals(0L);
        RequestStatus status = needConfirmation && hasParticipantsLimit ? RequestStatus.PENDING : RequestStatus.CONFIRMED;
        return ParticipationRequest.builder()
                .requester(user)
                .status(status)
                .event(event)
                .build();
    }

    private User checkUserExistAndGet(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));
    }
}
