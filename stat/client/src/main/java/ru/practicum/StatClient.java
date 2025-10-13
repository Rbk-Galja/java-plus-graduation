package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;
import ru.practicum.dto.RequestHitDto;
import ru.practicum.dto.ResponseDto;


import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatClient {
    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;
    private final RestTemplate restTemplate;

    @Value("${stat.service-id:stats-server}")
    private String statsServiceId;

    private ServiceInstance getInstance() {
        List<ServiceInstance> instances = discoveryClient.getInstances(statsServiceId);
        return instances.stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No instances of " + statsServiceId));
    }

    private URI makeUri(String path) {
        ServiceInstance instance = retryTemplate.execute(ctx -> getInstance());
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort() + path);
    }

    public void sendHit(RequestHitDto hit) {
        log.info("Вызов записи хита в клиенте");
        try {
            URI uri = makeUri("/hit");
            restTemplate.postForEntity(uri, hit, Void.class);
        } catch (Exception e) {
            log.warn("Ошибка при записи статистики: {}", e.getMessage());
        }
    }

    public List<ResponseDto> getStats(String start, String end, List<String> uris, boolean unique) {
        log.info("Вызов получения статистики в клиенте");
        try {
            String urisParam = uris == null ? "" : uris.stream().collect(Collectors.joining(","));
            String q = String.format("/stats?start=%s&end=%s&uris=%s&unique=%s",
                    UriUtils.encodePath(start, StandardCharsets.UTF_8),
                    UriUtils.encodePath(end, StandardCharsets.UTF_8),
                    UriUtils.encodePath(urisParam, StandardCharsets.UTF_8),
                    unique);

            URI uri = makeUri(q);
            ResponseEntity<List<ResponseDto>> resp = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ResponseDto>>() {}
            );
            return resp.getBody() == null ? Collections.emptyList() : resp.getBody();
        } catch (Exception e) {
            log.warn("Ошибка при получении статистики: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}