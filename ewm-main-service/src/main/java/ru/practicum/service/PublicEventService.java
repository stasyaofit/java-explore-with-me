package ru.practicum.service;

import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventFilterParamsDto;
import ru.practicum.dto.event.EventFullDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface PublicEventService {

    List<EventDto> getEventsByPublic(EventFilterParamsDto params, HttpServletRequest request);

    EventFullDto getEventByPublic(Long eventId, HttpServletRequest request);
}