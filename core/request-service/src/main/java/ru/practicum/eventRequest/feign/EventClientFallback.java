package ru.practicum.eventRequest.feign;

import feign.FeignException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.event.EventDto;
import ru.practicum.exeption.NotFoundException;


@Component
public class EventClientFallback implements EventClient {

    @Override
    public boolean findByIdAndInitiatorId(Long eventId, Long userId) throws FeignException {
        return true;
    }

    @Override
    public EventDto findById(Long id) throws FeignException {
        throw new NotFoundException("Event", id);
    }

    @Override
    public boolean existsByCategoryId(@RequestParam Long id) throws FeignException {
        return true;
    }
}
