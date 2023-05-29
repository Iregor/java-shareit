package ru.practicum.shareit.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@Constraint(validatedBy = ItemAvailableValidator.class)
@Documented
public @interface ItemAvailable {
    String message() default "Item not available.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
