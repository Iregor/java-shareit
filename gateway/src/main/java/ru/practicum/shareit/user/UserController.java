package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody @Valid UserDto user) {
        return userClient.createUser(user);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> findUserById(@PathVariable @Positive Long userId) {
        return userClient.findUserById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> findAllUsers() {
        return userClient.findAllUsers();
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> patchUser(@RequestBody UserDto userDto, @PathVariable @Positive Long userId) throws IllegalAccessException {
        return userClient.patchUser(userDto, userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable @Positive Long userId) {
        userClient.deleteUserById(userId);
    }
}