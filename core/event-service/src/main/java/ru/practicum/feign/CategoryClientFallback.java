package ru.practicum.feign;

import feign.FeignException;
import org.springframework.stereotype.Component;
import ru.practicum.dto.category.CategoryDto;

@Component
public class CategoryClientFallback implements CategoryClient {

    @Override
    public CategoryDto findById(Long id) throws FeignException {
        return createCategoryDtoWithId(id);
    }

    private CategoryDto createCategoryDtoWithId(Long id) {
        CategoryDto cat = new CategoryDto();
        cat.setId(id);
        return cat;
    }
}
