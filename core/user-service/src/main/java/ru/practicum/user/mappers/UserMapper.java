package ru.practicum.user.mappers;

import org.mapstruct.Mapper;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User mapToUser(UserDto userDto);

    UserDto mapToUserDto(User user);

    UserShortDto mapToUserShortDto(User user);
}
