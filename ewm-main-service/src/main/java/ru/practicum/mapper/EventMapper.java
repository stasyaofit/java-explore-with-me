package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.event.EventFilterParams;
import ru.practicum.dto.event.EventFilterParamsDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class, LocationMapper.class})
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventDate", expression = "java(newEventDto.getEventDate())")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    Event mapToEvent(NewEventDto newEventDto);

    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "createdOn", expression = "java(ru.practicum.util.DateTimeFormatter.mapLocalDateTimeToString(event.getEventDate()))")
    @Mapping(target = "eventDate", expression = "java(ru.practicum.util.DateTimeFormatter.mapLocalDateTimeToString(event.getEventDate()))")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "publishedOn", expression = "java(ru.practicum.util.DateTimeFormatter.mapLocalDateTimeToString(event.getPublishedOn()))")
    @Mapping(target = "views", source = "views")
    EventFullDto mapToEventFullDto(Event event);

    @Mapping(target = "category", source = "category")
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "eventDate", expression = "java(ru.practicum.util.DateTimeFormatter.mapLocalDateTimeToString(event.getEventDate()))")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "views", ignore = true)
    EventShortDto mapToEventShortDto(Event event);

    @Mapping(target = "rangeStart", expression = "java(start)")
    @Mapping(target = "rangeEnd", expression = "java(end)")
    EventFilterParams mapToEventFilterParams(EventFilterParamsDto filterDto, LocalDateTime start, LocalDateTime end);

    List<EventShortDto> mapToEventShortDtoListForEvents(List<Event> events);
}
