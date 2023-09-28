package ru.practicum.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.event.EventFilterParamsDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.service.AdminEventService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Slf4j
public class AdminEventController {

    private final AdminEventService service;

    @GetMapping
    public List<EventFullDto> getEventsByAdmin(@Valid EventFilterParamsDto params) {
        log.info("Получен GET-запрос к эндпоинту: /admin/events на получение списка всех событий администратором.");
        return service.getEventsByAdmin(params);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable Long eventId,
                                      @Valid @RequestBody UpdateEventAdminRequest request) {
        log.info("Получен PATCH-запрос к эндпоинту: /admin/events/{eventId} " +
                "на обновление события с id = {} администратором.", eventId);
        return service.updateEventByAdmin(request, eventId);
    }
}