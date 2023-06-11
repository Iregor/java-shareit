package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exception.exceptions.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Validated
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final Validator validator;

    @Override
    public UserDto createUser(User user) {
        return UserMapper.toDto(repository.save(user));
    }

    @Override
    public UserDto findUserById(Long userId) {
        return UserMapper.toDto(repository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found.", userId, String.valueOf(Thread.currentThread().getStackTrace()[1]))));
    }

    @Override
    public List<UserDto> findAllUsers() {
        return repository.findAll().stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public UserDto patchUser(User user, Long userId) throws IllegalAccessException {
        buildUserEntity(user, userId);
        validateUser(user);
        return UserMapper.toDto(repository.save(user));
    }

    @Override
    public void deleteUserById(Long userId) {
        repository.deleteById(userId);
    }

    private void buildUserEntity(User user, Long userId) throws IllegalAccessException {
        User repoUser = repository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found.", user.getId(), String.valueOf(Thread.currentThread().getStackTrace()[1])));

        Class<? extends User> cls = user.getClass();
        for (Field field : cls.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.get(user) == null) {
                field.set(user, field.get(repoUser));
            }
        }
    }

    private void validateUser(User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        assertUniqueEmail(user);
    }

    private void assertUniqueEmail(User user) {
        Optional<User> userOpt = repository.findByEmail(user.getEmail());
        if (userOpt.isPresent() && !userOpt.get().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
    }
}
