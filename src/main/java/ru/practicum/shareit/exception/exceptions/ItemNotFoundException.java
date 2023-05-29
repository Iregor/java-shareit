package ru.practicum.shareit.exception.exceptions;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ItemNotFoundException extends RuntimeException {
    private final Long itemId;
    private final String backInfo;
    private final LocalDateTime time;

    public ItemNotFoundException(Long itemId, String backInfo) {
        super(String.format("Item id: %d not found.", itemId));
        time = LocalDateTime.now();
        this.itemId = itemId;
        this.backInfo = backInfo;
    }
}
