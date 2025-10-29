package ru.practicum.eventRequest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.request.EventRequestDto;
import ru.practicum.eventRequest.model.EventRequest;
import ru.practicum.helper.RequestParamHelper;

@Mapper(componentModel = "spring")
public interface EventRequestMapper {
    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "requester", source = "requesterId")
    @Mapping(target = "created", dateFormat = RequestParamHelper.DATE_TIME_FORMAT)
    EventRequestDto mapToEventRequestDto(EventRequest eventRequest);
}
