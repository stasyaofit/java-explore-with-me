package ru.practicum.controller.pub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.event.EventFilterParamsDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.service.PublicEventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@Slf4j
public class PublicEventController {
    private final PublicEventService service;

    @GetMapping
    public List<EventShortDto> getAll(@Valid EventFilterParamsDto params, HttpServletRequest request) {
        log.info("Получен GET-запрос к эндпоинту: /events на получение списка всех событий.");
        return service.getEventsByPublic(params, request);
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        log.info("Получен GET-запрос к эндпоинту: /events/{id} на получение события с id = {}.", id);
        return service.getEventByPublic(id, request);
    }
}