package ru.practicum.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.Objects;

@UtilityClass
public class DateTimeFormatter {
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public String mapLocalDateTimeToString(LocalDateTime localDateTime) {
        return Objects.isNull(localDateTime) ?
                null : localDateTime.format(java.time.format.DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }

    public static LocalDateTime mapStringToLocalDateTime(String localDateTime) {
        if (localDateTime != null) {
            return LocalDateTime.parse(localDateTime, java.time.format.DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        }
        return null;
    }
}