package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;
import ru.practicum.dto.location.LocationDto;

import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static ru.practicum.util.DateTimeFormatter.DATE_TIME_FORMAT;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {
    @NotBlank
    @Length(min = 20, max = 2000)
    String annotation;
    @NotNull
    Long category;
    @Length(min = 20, max = 7000)
    @NotBlank
    String description;
    @NotNull
    @Future
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    LocalDateTime eventDate;
    @NotNull
    @Valid
    LocationDto location;
    @NotNull
    Boolean paid = Boolean.FALSE;
    @Min(0)
    @NotNull
    Integer participantLimit = 0;
    @NotNull
    Boolean requestModeration = Boolean.TRUE;
    @Length(min = 3, max = 120)
    @NotBlank
    String title;
}