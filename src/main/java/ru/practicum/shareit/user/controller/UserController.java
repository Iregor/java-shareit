package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.validation.Exist;
import ru.practicum.shareit.validation.UniqueUser;

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
    public UserDto createUser(@RequestBody @UniqueUser @Valid User user) {
        return service.createUser(user);
    }

    @GetMapping("/{userId}")
    public UserDto findUserById(@PathVariable @Exist("user") Long userId) {
        return service.findUserById(userId);
    }

    @GetMapping
    public List<UserDto> findAllUsers() {
        return service.findAllUsers();
    }

    @PutMapping
    public UserDto updateUser(@RequestBody @Exist("user") @UniqueUser @Valid User user) {
        return service.updateUser(user);
    }

    @PatchMapping("/{userId}")
    public UserDto patchUser(@RequestBody User user, @PathVariable @Exist("user") Long userId) throws IllegalAccessException {
        user.setId(userId);
        return service.patchUser(user);
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable @Exist("user") Long userId) {
        service.deleteUserById(userId);
    }
}