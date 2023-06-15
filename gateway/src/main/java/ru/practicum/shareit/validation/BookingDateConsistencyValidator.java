package ru.practicum.shareit.validation;

import ru.practicum.shareit.booking.dto.BookingDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class BookingDateConsistencyValidator implements ConstraintValidator<BookingDateConsistency, BookingDto> {

    @Override
    public boolean isValid(BookingDto value, ConstraintValidatorContext context) {
        if (value == null || value.getStart() == null || value.getEnd() == null) {
            return true;
        }
        return value.getEnd().isAfter(value.getStart());
    }
}
