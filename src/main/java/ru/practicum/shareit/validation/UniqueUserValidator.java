package ru.practicum.shareit.validation;

import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

@RequiredArgsConstructor
public class UniqueUserValidator implements ConstraintValidator<UniqueUser, User> {
    private final UserRepository userRepository;

    @Override
    public boolean isValid(User user, ConstraintValidatorContext constraintValidatorContext) {
        Optional<User> userOpt = userRepository.findUserByEmail(user.getEmail());
        //Another user with such email not found, or current user was found (with user.id)
        return userOpt.isEmpty() || userOpt.get().getId().equals(user.getId());
    }
}
