package ru.practicum.feign;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.user.UserShortDto;


public interface UserClientOperations {

    @GetMapping
    UserShortDto getUserById(@RequestParam Long id) throws FeignException;

    @GetMapping("/exist")
    boolean userExistById(@RequestParam Long id) throws FeignException;
}
