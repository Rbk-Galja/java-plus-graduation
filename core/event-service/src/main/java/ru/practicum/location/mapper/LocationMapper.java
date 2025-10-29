package ru.practicum.location.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.location.dto.NewLocationRequest;
import ru.practicum.location.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    @Mapping(target = "id", ignore = true)
    Location mapToLocationNew(NewLocationRequest request);

    @Mapping(target = "id", ignore = true)
    Location mapToFull(LocationDto dto);
}
