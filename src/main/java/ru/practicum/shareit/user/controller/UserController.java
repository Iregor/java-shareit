package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService service;

    @PostMapping
    public UserDto createUser(@RequestBody @Valid User user) {
        return service.createUser(user);
    }

    @GetMapping("/{userId}")
    public UserDto findUserById(@PathVariable Long userId) {
        return service.findUserById(userId);
    }

    @GetMapping
    public List<UserDto> findAllUsers() {
        return service.findAllUsers();
    }

    @PatchMapping("/{userId}")
    public UserDto patchUser(@RequestBody User user, @PathVariable Long userId) throws IllegalAccessException {
        return service.patchUser(user, userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) {
        service.deleteUserById(userId);
    }
}