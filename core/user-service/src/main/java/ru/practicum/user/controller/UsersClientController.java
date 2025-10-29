package ru.practicum.user.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.feign.UserClientOperations;
import ru.practicum.user.service.UserService;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/user")
public class UsersClientController implements UserClientOperations {
    private final UserService userService;

    @Override
    public UserShortDto getUserById(Long id) throws FeignException {
        log.info("___Получаем по id = {} User в UsersClientController", id);
        return userService.getById(id);
    }

    @Override
    public boolean userExistById(Long id) throws FeignException {
        return userService.userExistById(id);
    }

}
