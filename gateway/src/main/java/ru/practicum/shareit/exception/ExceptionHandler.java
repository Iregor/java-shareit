package ru.practicum.shareit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;


@RestController
@ControllerAdvice
@Slf4j
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Response> handleIllegalArgumentException(IllegalArgumentException exc) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(exc.getMessage()));

    }

/*    @org.springframework.web.bind.annotation.ExceptionHandler(Throwable.class)
    ResponseEntity<Response> handleThrowableException(Throwable exc) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(exc.getMessage() != null ? exc.getMessage() : null));
    }*/

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class Response {
        private String error;
    }
}
