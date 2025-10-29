package ru.practicum.feign;

import feign.FeignException;
import org.springframework.stereotype.Component;
import ru.practicum.dto.user.UserShortDto;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public UserShortDto getUserById(Long id) throws FeignException {
        return createShortDtoWithId(id);
    }

    @Override
    public boolean userExistById(Long id) throws FeignException {
        return true;
    }

    private UserShortDto createShortDtoWithId(Long id) {
        UserShortDto dto = new UserShortDto();
        dto.setId(id);
        return dto;
    }
}
