package ru.practicum.feign;

import feign.FeignException;
import org.springframework.stereotype.Component;
import ru.practicum.dto.request.Status;

@Component
public class RequestClientFallback implements RequestClient {

    @Override
    public boolean findByRequesterIdAndEventIdAndStatus(Long requesterId, Long eventId, Status status) {
        return false;
    }

    @Override
    public Long countRequestsByEventAndStatus(Long id, Status status) throws FeignException {
        return 0L;
    }

    @Override
    public Long countRequestByEvent(Long id) throws FeignException {
        return 0L;
    }
}
