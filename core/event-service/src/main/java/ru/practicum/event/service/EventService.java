package ru.practicum.event.service;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.request.Status;
import ru.practicum.event.dto.*;

import java.util.List;

public interface EventService {
    EventDto addEvent(Long userId, NewEventRequest request);

    EventDto updateEventByUser(Long userId, Long eventId, UpdateEventRequest request);

    EventDto updateEventByAdmin(Long eventId, UpdateEventRequest request);

    EventDto getByIdPublic(Long eventId);

    List<EventShortDto> getUsersEvents(Long userId, Pageable page);

    EventDto getByIdPrivate(Long userId, Long eventId);

    List<EventDto> getEventsWithParamAdmin(EventSearchParam eventSearchParam, Pageable page);

    List<EventShortDto> getEventsWithParamPublic(EventSearchParam eventSearchParam, Pageable page);

    EventDto getById(Long id);

    boolean getByEventIdAndUserId(Long eventId, Long userId);

    boolean existsByCategoryId(@RequestParam Long id);

    Long countRequestConfirmedByEventDto(Long eventId, Status status);

    void sendLike(Long eventId, Long userId);

    List<EventDto> getRecommendation(Long userId, int maxResult);
}
