package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;
import ru.practicum.dto.location.LocationDto;

import javax.validation.constraints.Min;
import java.time.LocalDateTime;

import static ru.practicum.util.DateTimeFormatter.DATE_TIME_FORMAT;


@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PROTECTED)
public class UpdateEventRequest {
    @Length(min = 20, max = 2000)
    String annotation;
    Long category;
    @Length(min = 20, max = 7000)
    String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    LocalDateTime eventDate;
    LocationDto location;
    Boolean paid;
    @Min(0)
    Long participantLimit;
    Boolean requestModeration;
    @Length(min = 3, max = 120)
    String title;
}