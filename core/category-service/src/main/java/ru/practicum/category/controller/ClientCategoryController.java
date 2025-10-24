package ru.practicum.category.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.category.service.CategoryService;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.feign.CategoryClientOperations;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/category")
public class ClientCategoryController implements CategoryClientOperations {

    private final CategoryService categoryService;

    @Override
    public CategoryDto findById(@RequestParam Long id) throws FeignException {
        return categoryService.findById(id);
    }
}
