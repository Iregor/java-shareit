package ru.practicum.shareit.exception.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.exception.exceptions.*;
import ru.practicum.shareit.exception.response.*;

import javax.validation.ConstraintViolationException;

@RestController
@ControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<?> handleConstraintViolation(ConstraintViolationException exc) {
        log.warn(ExceptionMapper.getLog(exc));
        return ResponseEntity.status(ExceptionMapper.getHttpStatus(exc)).body(ExceptionMapper.getResponseBody(exc));
    }

    @ExceptionHandler(IllegalAccessToEntityException.class)
    ResponseEntity<IllegalAccessToEntityResponse> handleIllegalAccessToItemException(IllegalAccessToEntityException exc) {
        log.warn(String.format("%s : %s : %d : %d : %s", exc.getTime(), exc.getMessage(), exc.getEntityId(), exc.getUserId(), exc.getBackInfo()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new IllegalAccessToEntityResponse(exc.getTime(), exc.getMessage(), exc.getEntityId(), exc.getUserId()));
    }

    @ExceptionHandler(ItemIdNotConsistentException.class)
    ResponseEntity<ItemIdNotConsistentResponse> handleItemIdNotConsistentException(ItemIdNotConsistentException exc) {
        log.warn(String.format("%s : %s : %d : %d : %s", exc.getMessage(), exc.getItemDto(), exc.getUserId(), exc.getItemId(), exc.getBackInfo()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ItemIdNotConsistentResponse(exc.getMessage(), exc.getItemDto(), exc.getUserId(), exc.getItemId()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    ResponseEntity<EntityNotFoundResponse> handleEntityNotFoundException(EntityNotFoundException exc) {
        log.warn(String.format("%s : %s : %s : %s", exc.getTime(), exc.getMessage(), exc.getEntityId(), exc.getBackInfo()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new EntityNotFoundResponse(exc.getTime(), exc.getMessage(), exc.getEntityId()));
    }

    @ExceptionHandler(BookingStatusAlreadyApprovedException.class)
    ResponseEntity<BookingStatusAlreadyApprovedResponse> handleEntityNotFoundException(BookingStatusAlreadyApprovedException exc) {
        log.warn(String.format("%s : %s : %s : %s", exc.getTime(), exc.getMessage(), exc.getBookingId(), exc.getBackInfo()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new BookingStatusAlreadyApprovedResponse(exc.getTime(), exc.getMessage(), exc.getBookingId()));
    }

    @ExceptionHandler(UnknownStateException.class)
    ResponseEntity<UnknownStateResponse> handleUnknownStateException(UnknownStateException exc) {
        log.warn(String.format("%s : %s : %s : %s", exc.getTime(), exc.getMessage(), exc.getState(), exc.getBackInfo()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UnknownStateResponse(exc.getTime(), exc.getMessage()));
    }

    @ExceptionHandler(NoResolvedBookingException.class)
    ResponseEntity<NoResolvedBookingResponse> noResolvedBookingExceptionHandler(NoResolvedBookingException exc) {
        log.warn(String.format("%s : %s : %s : %s : %s", exc.getTime(), exc.getMessage(), exc.getItemId(), exc.getUserId(), exc.getBackInfo()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new NoResolvedBookingResponse(exc.getTime(), exc.getMessage(), exc.getItemId(), exc.getUserId()));
    }
}
