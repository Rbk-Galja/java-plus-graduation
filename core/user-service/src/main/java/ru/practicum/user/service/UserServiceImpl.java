package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exeption.EmailMustBeUniqueException;
import ru.practicum.exeption.UserNotExistException;
import ru.practicum.user.mappers.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new EmailMustBeUniqueException(userDto.getEmail());
        }
        return userMapper.mapToUserDto(userRepository.save(userMapper.mapToUser(userDto)));
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Long from, Long size) {
        List<User> users;

        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findByIdIn(ids);
        } else {
            Page<User> page = userRepository.findByIdAfter(
                    from,
                    PageRequest.of(0, size.intValue(), Sort.by("id"))
            );
            users = page.getContent();
        }

        return users.stream()
                .map(userMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotExistException(userId);
        }
        userRepository.deleteById(userId);
    }

    @Override
    public UserShortDto getById(Long userId) {
        return userMapper.mapToUserShortDto(userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExistException(userId)));
    }

    @Override
    public boolean userExistById(Long id) {
        return userRepository.existsById(id);
    }
}
