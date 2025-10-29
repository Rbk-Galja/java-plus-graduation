package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "category-service", path = "/api/v1/category",
        fallback = CategoryClientFallback.class)
public interface CategoryClient extends CategoryClientOperations {
}
