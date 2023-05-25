package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
@Validated
public class UserRepositoryMemory implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private Long currentId = 0L;

    @Override
    public Optional<User> createUser(User user) {
        user.setId(getNewId());
        users.put(user.getId(), user);
        return Optional.ofNullable(users.get(currentId));
    }

    @Override
    public Optional<User> findUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public List<User> findAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return users.values().stream().filter(user -> user.getEmail().equals(email)).findFirst();
    }

    @Override
    public Optional<User> updateUser(User user) {
        users.put(user.getId(), user);
        return Optional.ofNullable(users.get(user.getId()));
    }

    @Override
    public void deleteUserById(Long userId) {
        users.remove(userId);
    }

    @Override
    public boolean assertUserExists(Long userId) {
        return users.get(userId) != null;
    }

    private Long getNewId() {
        return ++currentId;
    }
}
