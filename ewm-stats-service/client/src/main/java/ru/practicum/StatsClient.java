package ru.practicum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.exception.BadRequestException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static ru.practicum.util.Constants.DATE_TIME_FORMATTER;

@Service
public class StatsClient extends BaseClient {

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public void  addHit(String app, String uri, String ip, LocalDateTime timestamp) {
        EndpointHit endpointHit = new EndpointHit(app, uri, ip, mapToString(timestamp));
        makeAndSendRequest(HttpMethod.POST, "/hit", null, endpointHit);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        Map<String, Object> parameters = new HashMap<>();
        if (isNull(start) || isNull(end) || end.isBefore(start)) {
            throw new BadRequestException("Параметры ограничения времени: начало и конец не должны быть null" +
                    " конец должен быть после начала.");
        }
        parameters.put("start", mapToString(start));
        parameters.put("end", mapToString(end));
        StringJoiner pathBuilder = new StringJoiner("&", "/stats?start={start}&end={end}", "");
        if (nonNull(uris) && !uris.isEmpty()) {
            uris.forEach(uri -> pathBuilder.add("&uris=" + uri));
        }
        if (nonNull(unique)) {
            pathBuilder.add("&unique=" + unique);
        }
        String path = pathBuilder.toString();
        return makeAndSendRequest(HttpMethod.GET, path, parameters, null);
    }

    private String mapToString(LocalDateTime timestamp) {
        return timestamp.format(DATE_TIME_FORMATTER);
    }
}
