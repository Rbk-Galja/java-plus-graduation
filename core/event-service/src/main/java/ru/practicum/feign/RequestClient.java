package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "request-service", path = "/api/v1/request",
        fallback = RequestClientFallback.class)
public interface RequestClient extends RequestClientOperations {
}
