package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> createUser(User user);

    Optional<User> findUserById(Long userId);

    List<User> findAllUsers();

    Optional<User> findUserByEmail(String email);

    Optional<User> updateUser(User user);

    void deleteUserById(Long userId);

    boolean assertUserExists(Long id);
}
