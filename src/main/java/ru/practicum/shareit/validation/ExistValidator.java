package ru.practicum.shareit.validation;

import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@RequiredArgsConstructor
public class ExistValidator implements ConstraintValidator<Exist, Object> {
    private String value;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

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
                return userRepository.assertUserExists(id);
            case "item":
                if (o instanceof Item) {
                    id = ((Item) o).getId();
                } else if (o instanceof Long) {
                    id = (Long) o;
                } else {
                    return false;
                }
                return itemRepository.assertItemExists(id);
        }
        return false;
    }
}
