package ru.practicum.shareit.validation;

import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepositoryJPA;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepositoryJPA;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@RequiredArgsConstructor
public class ExistValidator implements ConstraintValidator<Exist, Object> {
    private String value;
    private final UserRepositoryJPA userRepository;
    private final ItemRepositoryJPA itemRepository;

    @Override
    public void initialize(Exist constraintAnnotation) {
        value = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
        Long id;
        switch (value) {
            case "user":
                if (o instanceof User) {
                    id = ((User) o).getId();
                } else if (o instanceof Long) {
                    id = (Long) o;
                } else {
                    return false;
                }
                return userRepository.existsById(id);
            case "item":
                if (o instanceof Item) {
                    id = ((Item) o).getId();
                } else if (o instanceof Long) {
                    id = (Long) o;
                } else {
                    return false;
                }
                return itemRepository.existsById(id);
        }
        return false;
    }
}
