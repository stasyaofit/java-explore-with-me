package ru.practicum.controller.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.PrivateEventService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class PrivateEventController {
    private final PrivateEventService service;

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId,
                            @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Получен POST-запрос к эндпоинту: /users/{userId}/events на добавление события от пользователя с id = {}.", userId);
        return service.createEventByPrivate(newEventDto, userId);
    }

    @GetMapping("/{userId}/events")
    public List<EventDto> getAll(@PathVariable Long userId,
                                 @RequestParam(defaultValue = "0") Integer from,
                                 @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен GET-запрос к эндпоинту: /users/{userId}/events на получение списка событий от пользователя с id = {}.", userId);
        return service.getEventsByPrivate(userId, from, size);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getEvent(@PathVariable Long userId,
                                 @PathVariable Long eventId) {
        log.info("Получен GET-запрос к эндпоинту: /users/{userId}/events/{eventId} на получение события с id = {} " +
                "от пользователя с id = {}.", eventId, userId);
        return service.getEventByPrivate(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                               @PathVariable Long eventId,
                               @Valid @RequestBody UpdateEventUserRequest updateRequest) {
        log.info("Получен PATCH-запрос к эндпоинту: /users/{userId}/events/{eventId} на обновление события с id = {} " +
                "от пользователя с id = {}.", eventId, userId);
        return service.updateEventByPrivate(updateRequest, userId, eventId);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequests(@PathVariable Long userId,
                                                     @PathVariable Long eventId) {
        log.info("Получен GET-запрос к эндпоинту: /users/{userId}/events/{eventId}/requests на получение списка запросов " +
                "на участие в событии с id = {} от пользователя с id = {}.", eventId, userId);
        return service.getRequestsByPrivate(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable Long userId,
                                                 @PathVariable Long eventId,
                                                 @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        log.info("Получен PATCH-запрос к эндпоинту: /users/{userId}/events/{eventId}/requests на обновление статуса запроса " +
                "на участие в событии с id = {} от пользователя с id = {}.", eventId, userId);
        return service.updateByPrivate(updateRequest, userId, eventId);
    }
}
