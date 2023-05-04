package ru.practicum.shareit.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
@Constraint(validatedBy = ExistValidator.class)
public @interface Exist {
    String value();

    String message() default "Provided entity does not exist.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
