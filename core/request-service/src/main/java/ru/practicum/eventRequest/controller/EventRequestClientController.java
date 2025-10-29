package ru.practicum.eventRequest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.request.Status;
import ru.practicum.eventRequest.service.EventRequestService;
import ru.practicum.feign.RequestClientOperations;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/request")
public class EventRequestClientController implements RequestClientOperations {

    private final EventRequestService eventRequestService;

    @Override
    public boolean findByRequesterIdAndEventIdAndStatus(Long requesterId, Long eventId, Status status) {
        return eventRequestService.findByRequesterIdAndEventIdAndStatus(requesterId, eventId, status);
    }

    @Override
    public Long countRequestsByEventAndStatus(Long id, Status status) {
        return eventRequestService.countRequestsByEventAndStatus(id, status);
    }

    @Override
    public Long countRequestByEvent(Long id) {
        return eventRequestService.countRequestByEvent(id);
    }
}
