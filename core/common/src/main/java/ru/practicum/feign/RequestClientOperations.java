package ru.practicum.feign;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.request.Status;

public interface RequestClientOperations {

    @GetMapping("/exist")
    boolean findByRequesterIdAndEventIdAndStatus(@RequestParam Long requesterId, @RequestParam Long eventId,
                                                 @RequestParam Status status) throws FeignException;

    @GetMapping("/count")
    Long countRequestsByEventAndStatus(@RequestParam Long id, @RequestParam Status status) throws FeignException;

    @GetMapping("/count/admin")
    Long countRequestByEvent(@RequestParam Long id) throws FeignException;
}
