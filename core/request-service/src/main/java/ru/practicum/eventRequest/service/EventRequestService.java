package ru.practicum.eventRequest.service;

import ru.practicum.dto.request.EventRequestDto;
import ru.practicum.dto.request.Status;
import ru.practicum.eventRequest.dto.EventRequestUpdateDto;
import ru.practicum.eventRequest.dto.EventRequestUpdateResult;

import java.util.List;

public interface EventRequestService {
    List<EventRequestDto> getUsersRequests(Long userId);

    EventRequestDto createRequest(Long userId, Long eventId);

    EventRequestDto cancelRequest(Long userId, Long requestId);

    List<EventRequestDto> getAllByEventId(Long userId, Long eventId);

    EventRequestUpdateResult updateRequestState(Long userId, Long eventId, EventRequestUpdateDto updateDto);

    boolean findByRequesterIdAndEventIdAndStatus(Long requesterId, Long eventId, Status status);

    Long countRequestsByEventAndStatus(Long id, Status status);

    Long countRequestByEvent(Long id);
}
