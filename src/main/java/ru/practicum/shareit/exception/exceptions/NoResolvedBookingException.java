package ru.practicum.shareit.exception.exceptions;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoResolvedBookingException extends RuntimeException {
    private Long itemId;
    private Long userId;
    private LocalDateTime time;
    private String backInfo;

    public NoResolvedBookingException(Long itemId, Long userId, String backInfo) {
        this.itemId = itemId;
        this.userId = userId;
        this.backInfo = backInfo;
        this.time = LocalDateTime.now();
    }

    public NoResolvedBookingException(String message, Long itemId, Long userId, String backInfo) {
        super(message);
        this.itemId = itemId;
        this.userId = userId;
        this.backInfo = backInfo;
        this.time = LocalDateTime.now();
    }
}
