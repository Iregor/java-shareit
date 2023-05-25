package ru.practicum.shareit.exception.controller;

import org.springframework.http.HttpStatus;
import ru.practicum.shareit.exception.exceptions.Violation;
import ru.practicum.shareit.exception.response.ValidationErrorResponse;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExceptionMapper {
    public static Object getResponseBody(Exception exc) {
        if (exc instanceof ConstraintViolationException) {
            return mapToConstraintViolationResponse(exc);
        }
        return null;
    }

    public static String getLog(Exception exc) {
        if (exc instanceof ConstraintViolationException) {
            return mapToConstraintViolationLog(exc);
        }
        return null;
    }

    public static HttpStatus getHttpStatus(Exception exc) {
        if (exc instanceof ConstraintViolationException) {
            return mapToConstraintViolationStatus(exc);
        }
        return null;
    }

    private static ValidationErrorResponse mapToConstraintViolationResponse(Exception exc) {
        ConstraintViolationException typeExc = (ConstraintViolationException) exc;
        Set<ConstraintViolation<?>> constraintViolations = typeExc.getConstraintViolations();
        String invalidEntity = new ArrayList<>(constraintViolations).get(0).getInvalidValue().toString();

        List<Violation> violations = constraintViolations.stream()
                .map(constraintViolation -> new Violation(constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage()))
                .collect(Collectors.toList());
        return new ValidationErrorResponse(invalidEntity, violations);
    }

    private static String mapToConstraintViolationLog(Exception exc) {
        ConstraintViolationException typeExc = (ConstraintViolationException) exc;
        Set<ConstraintViolation<?>> constraintViolations = typeExc.getConstraintViolations();
        String invalidEntity = new ArrayList<>(constraintViolations).get(0).getInvalidValue().toString();

        List<Violation> violations = constraintViolations.stream()
                .map(constraintViolation -> new Violation(constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage()))
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder(String.format("invalidEntity: %s. ", invalidEntity));
        sb.append("Violations: ");
        for (Violation violation : violations) {
            sb.append(String.format("fieldName: %s / message: %s", violation.getFieldName(), violation.getMessage()));
        }
        return sb.toString();
    }

    private static HttpStatus mapToConstraintViolationStatus(Exception exc) {
        ConstraintViolationException typeExc = (ConstraintViolationException) exc;
        Set<ConstraintViolation<?>> constraintViolations = typeExc.getConstraintViolations();
        List<Violation> violations = constraintViolations.stream()
                .map(constraintViolation -> new Violation(constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage()))
                .collect(Collectors.toList());

        for (Violation violation : violations) {
            if (violation.getMessage().contains("Email already in use.")) {
                return HttpStatus.CONFLICT;
            }
            if (violation.getMessage().contains("Provided entity does not exist.")) {
                return HttpStatus.NOT_FOUND;
            }
        }
        return HttpStatus.BAD_REQUEST;
    }
}
