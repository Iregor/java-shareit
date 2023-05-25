package ru.practicum.shareit.exception.exceptions;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UnknownStateException extends RuntimeException {
    private String state;
    private String backInfo;
    private LocalDateTime time;

    public UnknownStateException(String state, String backInfo) {
        this.state = state;
        this.backInfo = backInfo;
        this.time = LocalDateTime.now();
    }

    public UnknownStateException(String message, String state, String backInfo) {
        super(message);
        this.state = state;
        this.backInfo = backInfo;
        this.time = LocalDateTime.now();
    }
}
