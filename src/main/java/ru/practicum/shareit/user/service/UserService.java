package ru.practicum.shareit.user.service;

import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Validated
public interface UserService {

    UserDto createUser(User user);

    UserDto findUserById(Long userId);

    List<UserDto> findAllUsers();

    UserDto updateUser(User user);

    UserDto patchUser(User user) throws IllegalAccessException;

    void deleteUserById(Long userId);
}
