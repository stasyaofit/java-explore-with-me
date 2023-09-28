package ru.practicum.dto.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.practicum.dto.location.LocationDto;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventFullDto extends EventDto {
    String description;
    Integer participantLimit;
    String state;
    String createdOn;
    String publishedOn;
    LocationDto location;
    Boolean requestModeration;
}