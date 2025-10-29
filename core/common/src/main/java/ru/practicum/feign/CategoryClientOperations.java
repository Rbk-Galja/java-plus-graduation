package ru.practicum.feign;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.category.CategoryDto;

public interface CategoryClientOperations {

    @GetMapping
    CategoryDto findById(@RequestParam Long id) throws FeignException;
}
