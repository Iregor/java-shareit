package ru.practicum.shareit.exception.controller;

import org.springframework.http.HttpStatus;
import ru.practicum.shareit.exception.exceptions.IllegalAccessToItemException;
import ru.practicum.shareit.exception.exceptions.ItemIdNotConsistentException;
import ru.practicum.shareit.exception.exceptions.Violation;
import ru.practicum.shareit.exception.response.IllegalAccessToItemResponse;
import ru.practicum.shareit.exception.response.ItemIdNotConsistentResponse;
import ru.practicum.shareit.exception.response.ValidationErrorResponse;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExceptionMapper {
    public static Object getResponseBody(Exception exc) {
        if (exc instanceof IllegalAccessToItemException) {
            return mapToIllegalAccessToItemResponse(exc);
        }
        if (exc instanceof ItemIdNotConsistentException) {
            return mapToItemIdNotConsistentResponse(exc);
        }
        if (exc instanceof ConstraintViolationException) {
            return mapToConstraintViolationResponse(exc);
        }
        return null;
    }

    public static String getLog(Exception exc) {
        if (exc instanceof IllegalAccessToItemException) {
            return mapToIllegalAccessToItemLog(exc);
        }
        if (exc instanceof ItemIdNotConsistentException) {
            return mapToItemIdNotConsistentLog(exc);
        }
        if (exc instanceof ConstraintViolationException) {
            return mapToConstraintViolationLog(exc);
        }
        return null;
    }

    public static HttpStatus getHttpStatus(Exception exc) {
        if (exc instanceof IllegalAccessToItemException) {
            return HttpStatus.FORBIDDEN;
        }
        if (exc instanceof ItemIdNotConsistentException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (exc instanceof ConstraintViolationException) {
            return mapToConstraintViolationStatus(exc);
        }
        return null;
    }

    private static IllegalAccessToItemResponse mapToIllegalAccessToItemResponse(Exception exc) {
        IllegalAccessToItemException typeExc = (IllegalAccessToItemException) exc;
        return new IllegalAccessToItemResponse(typeExc.getMessage(), typeExc.getItemDto(), typeExc.getUserId(), typeExc.getItemId());
    }

    private static String mapToIllegalAccessToItemLog(Exception exc) {
        IllegalAccessToItemException typeExc = (IllegalAccessToItemException) exc;
        return String.format("%s / %s / ItemDto: %s / headerUserId: %d / pathVarItemId: %d", typeExc.getBackInfo(), typeExc.getMessage(), typeExc.getItemDto(), typeExc.getUserId(), typeExc.getItemId());
    }

    private static ItemIdNotConsistentResponse mapToItemIdNotConsistentResponse(Exception exc) {
        ItemIdNotConsistentException typeExc = (ItemIdNotConsistentException) exc;
        return new ItemIdNotConsistentResponse(exc.getMessage(), typeExc.getItemDto(), typeExc.getUserId(), typeExc.getItemId());
    }

    private static String mapToItemIdNotConsistentLog(Exception exc) {
        ItemIdNotConsistentException typeExc = (ItemIdNotConsistentException) exc;
        return String.format("%s / %s / ItemDto: %s / headerUserId: %d / pathVarItemId: %d", typeExc.getBackInfo(), typeExc.getMessage(), typeExc.getItemDto(), typeExc.getUserId(), typeExc.getItemId());
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
        for (Violation violation :
                violations) {
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
