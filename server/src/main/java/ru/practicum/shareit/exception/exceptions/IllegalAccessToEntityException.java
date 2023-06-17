package ru.practicum.shareit.exception.exceptions;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class IllegalAccessToEntityException extends RuntimeException {

    private final String backInfo;

    private final Long entityId;
    private final Long userId;
    private final LocalDateTime time;

    public IllegalAccessToEntityException(String message, Long entityId, Long userId, String backInfo) {
        super(message);
        this.backInfo = backInfo;
        this.userId = userId;
        this.entityId = entityId;
        this.time = LocalDateTime.now();
    }
}
