package ru.practicum.shareit.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

//Annotation to prevent saving user with fields, which should be unique and already used by any other user;
//For example: email in this case. In the future, it could be login, nickname and others.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
@Constraint(validatedBy = UniqueUserValidator.class)
public @interface UniqueUser {

    String message() default "Email already in use. Try to sign up with another email.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
