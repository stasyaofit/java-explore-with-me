package ru.practicum.controller.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class PrivateRequestController {
    private final ParticipationRequestService service;

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getAll(@PathVariable Long userId) {
        log.info("Получен GET-запрос к эндпоинту: /users/{userId}/requests на получение списка всех запросов на участие.");
        return service.getAll(userId);
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequest(@PathVariable Long userId,
                                       @RequestParam Long eventId) {
        log.info("Получен POST-запрос к эндпоинту: /users/{userId}/requests на добавлниее запроса на участие.");
        return service.addParticipationRequest(userId, eventId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelParticipationRequest(@PathVariable Long userId,
                                          @PathVariable Long requestId) {
        log.info("Получен PATCH-запрос к эндпоинту: /users/{userId}/requests/{requestId}/cancel на отклонение запроса на участие.");
        return service.cancelParticipationRequest(userId, requestId);
    }

}
