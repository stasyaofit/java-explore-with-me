package ru.practicum.dto.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.enums.EventSort;

import java.util.Comparator;
import java.util.Objects;

import static ru.practicum.util.DateTimeFormatter.mapStringToLocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventDto implements Comparable<EventDto> {
    Long id;
    String title;
    String annotation;
    CategoryDto category;
    Boolean paid;
    String eventDate;
    UserShortDto initiator;
    Long views;
    Long confirmedRequests;

    @Override
    public int compareTo(EventDto other) {
        return this.id.compareTo(other.id);
    }

    public static final Comparator<EventDto> EVENT_DATE_COMPARATOR =
            Comparator.comparing((EventDto eventDto) -> mapStringToLocalDateTime(eventDto.eventDate) )
                    .thenComparing(EventDto::getId);

    public static final Comparator<EventDto> VIEWS_COMPARATOR =
            Comparator.comparing(EventDto::getViews)
                    .thenComparing(EventDto::getId);

    public static Comparator<EventDto> getEventComparator(EventSort sortType) {
        if (Objects.nonNull(sortType) && sortType == EventSort.VIEWS) {
            return VIEWS_COMPARATOR.reversed();
        }
        return EVENT_DATE_COMPARATOR.reversed();
    }
}