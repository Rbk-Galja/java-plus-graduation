package ru.practicum.event.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.event.EventDto;
import ru.practicum.event.service.EventService;
import ru.practicum.feign.EventClientOperations;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/event")
public class EventClientController implements EventClientOperations {
    private final EventService eventService;

    @Override
    public EventDto findById(Long id) throws FeignException {
        log.info("_____Получаем Event id = {} в EventClientController", id);
        return eventService.getById(id);
    }

    @Override
    public boolean findByIdAndInitiatorId(Long eventId, Long userId) throws FeignException {
        return eventService.getByEventIdAndUserId(eventId, userId);
    }

    @Override
    public boolean existsByCategoryId(@RequestParam Long id) throws FeignException {
        return eventService.existsByCategoryId(id);
    }
}
