package ru.practicum.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.dto.CategoryCreateDto;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto mapToDto(Category category);

    @Mapping(target = "id", ignore = true)
    Category toEntity(CategoryCreateDto dto);
}
