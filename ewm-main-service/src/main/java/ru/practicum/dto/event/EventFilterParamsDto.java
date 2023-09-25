package ru.practicum.dto.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.enums.EventSort;
import ru.practicum.enums.EventState;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventFilterParamsDto {
    List<Long> ids = List.of();
    List<EventState> states = List.of();
    List<Long> categories = List.of();
    String rangeStart;
    String rangeEnd;
    Integer from = 0;
    Integer size = 10;
    String text;
    Boolean paid;
    Boolean onlyAvailable = Boolean.FALSE;
    EventSort sort = EventSort.EVENT_DATE;
}
