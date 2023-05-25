package ru.practicum.shareit.validation;

import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.exception.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.repository.ItemRepositoryJPA;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@RequiredArgsConstructor
public class ItemAvailableValidator implements ConstraintValidator<ItemAvailable, Long> {
    private final ItemRepositoryJPA itemRepository;

    @Override
    public boolean isValid(Long itemId, ConstraintValidatorContext constraintValidatorContext) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found.", itemId, String.valueOf(Thread.currentThread().getStackTrace()[1])))
                .getAvailable();
    }
}
