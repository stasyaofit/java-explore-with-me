package ru.practicum.service;

import ru.practicum.dto.event.EventFilterParamsDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface PublicEventService {

    List<EventShortDto> getEventsByPublic(EventFilterParamsDto params, HttpServletRequest request);

    EventFullDto getEventByPublic(Long eventId, HttpServletRequest request);
}