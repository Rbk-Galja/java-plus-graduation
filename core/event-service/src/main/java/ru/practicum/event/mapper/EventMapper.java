package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventRequest;
import ru.practicum.event.model.Event;
import ru.practicum.helper.RequestParamHelper;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "categoryId", source = "category")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "participantLimit", ignore = true)
    @Mapping(target = "location", ignore = true)
    Event mapToEventNew(NewEventRequest request);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "initiator", source = "user")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "eventDate", dateFormat = RequestParamHelper.DATE_TIME_FORMAT)
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "rating", source = "rating")
    EventShortDto mapToShortDto(Event event, UserShortDto user, CategoryDto category, Long confirmedRequests,
                                double rating);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "createdOn", dateFormat = RequestParamHelper.DATE_TIME_FORMAT)
    @Mapping(target = "publishedOn", dateFormat = RequestParamHelper.DATE_TIME_FORMAT)
    @Mapping(target = "eventDate", dateFormat = RequestParamHelper.DATE_TIME_FORMAT)
    @Mapping(target = "initiator", source = "user")
    @Mapping(target = "category", source = "categoryDto")
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "rating", source = "rating")
    EventDto mapToFullDto(Event event, UserShortDto user, CategoryDto categoryDto, Long confirmedRequests,
                          double rating);
}
