package ru.practicum.shareit.exception.exceptions;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EntityNotFoundException extends RuntimeException {
    private Long entityId;
    private String backInfo;
    private LocalDateTime time;

    public EntityNotFoundException(Long entityId, String backInfo) {
        this.entityId = entityId;
        this.backInfo = backInfo;
        this.time = LocalDateTime.now();
    }

    public EntityNotFoundException(String message, Long entityId, String backInfo) {
        super(message);
        this.entityId = entityId;
        this.backInfo = backInfo;
        this.time = LocalDateTime.now();
    }
}
