package ru.practicum.shareit.exception.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.exception.exceptions.IllegalAccessToItemException;
import ru.practicum.shareit.exception.exceptions.ItemIdNotConsistentException;

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

    @ExceptionHandler(IllegalAccessToItemException.class)
    ResponseEntity<?> handleIllegalAccessToItemException(IllegalAccessToItemException exc) {
        log.warn(ExceptionMapper.getLog(exc));
        return ResponseEntity.status(ExceptionMapper.getHttpStatus(exc)).body(ExceptionMapper.getResponseBody(exc));
    }

    @ExceptionHandler(ItemIdNotConsistentException.class)
    ResponseEntity<?> handleItemIdNotConsistentException(ItemIdNotConsistentException exc) {
        log.warn(ExceptionMapper.getLog(exc));
        return ResponseEntity.status(ExceptionMapper.getHttpStatus(exc)).body(ExceptionMapper.getResponseBody(exc));
    }


}
