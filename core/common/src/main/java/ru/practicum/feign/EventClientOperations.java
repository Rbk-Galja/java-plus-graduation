package ru.practicum.feign;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.event.EventDto;

public interface EventClientOperations {

    @GetMapping("/initiation")
    boolean findByIdAndInitiatorId(@RequestParam Long eventId, @RequestParam Long userId) throws FeignException;

    @GetMapping("/{id}")
    EventDto findById(@PathVariable Long id) throws FeignException;

    @GetMapping("/category")
    boolean existsByCategoryId(@RequestParam Long id) throws FeignException;

}
