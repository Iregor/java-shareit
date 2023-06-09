package ru.practicum.shareit.exception.exceptions;

import lombok.Getter;
import ru.practicum.shareit.item.dto.ItemDto;

@Getter
public class ItemIdNotConsistentException extends RuntimeException {

    private final String backInfo;
    private final ItemDto itemDto;
    private final Long userId;
    private final Long itemId;

    public ItemIdNotConsistentException(String backInfo, String message, ItemDto itemDto, Long userId, Long itemId) {
        super(message);
        this.backInfo = backInfo;
        this.itemDto = itemDto;
        this.userId = userId;
        this.itemId = itemId;
    }
}
