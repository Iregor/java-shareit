package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exception.exceptions.RepositoryException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.validation.UniqueUser;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
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
        return UserMapper.toDto(repository.createUser(user).orElseThrow(() -> new RepositoryException(LocalDateTime.now() + " : " + Thread.currentThread().getStackTrace()[1] + String.format(" : fail to create user : %s.", user.toString()))));
    }

    @Override
    public UserDto findUserById(Long userId) {
        return UserMapper.toDto(repository.findUserById(userId).orElseThrow(() -> new RepositoryException(LocalDateTime.now() + " : " + Thread.currentThread().getStackTrace()[1] + String.format(" : fail to find user id : %d.", userId))));
    }

    @Override
    public List<UserDto> findAllUsers() {
        return repository.findAllUsers().stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public UserDto updateUser(User user) {
        return UserMapper.toDto(repository.updateUser(user).orElseThrow(() -> new RepositoryException(LocalDateTime.now() + " : " + Thread.currentThread().getStackTrace()[1] + String.format(" : fail to update user : %s.", user.toString()))));
    }

    @Override
    public UserDto patchUser(User user) throws IllegalAccessException {
        buildUserEntity(user);
        validateUser(user);
        return updateUser(user);
    }

    @Override
    public void deleteUserById(Long userId) {
        repository.deleteUserById(userId);
    }

    private void buildUserEntity(User user) throws IllegalAccessException {
        User repoUser = repository.findUserById(user.getId()).orElseThrow(() -> new RepositoryException(LocalDateTime.now() + " : " + Thread.currentThread().getStackTrace()[1] + String.format(" : fail to find user id : %d.", user.getId())));
        //идея с рефлексией понравилась тем, что:
        //1. давно хотелось ее попробовать
        //2. не придется переписывать код, если добавятся новые поля у класса User (в отличии от прямого поиска null полей по их названиям)
        Class<? extends User> cls = user.getClass();
        for (Field field : cls.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.get(user) == null) {
                field.set(user, field.get(repoUser));
            }
        }
    }

    private void validateUser(@UniqueUser User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        //пытался не использовать отдельный метод для уникального email, а сделать валидацию на аннотации
        //(использовать @UniqueUser на параметре метода validateUser, но spring в упор не хотел ее подхватывать
        //если сможешь - помоги пожалуйста:)
        assertUniqueEmail(user);

    }

    private void assertUniqueEmail(User user) {
        Optional<User> userOpt = repository.findUserByEmail(user.getEmail());
        if (userOpt.isPresent() && !userOpt.get().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
    }
}
