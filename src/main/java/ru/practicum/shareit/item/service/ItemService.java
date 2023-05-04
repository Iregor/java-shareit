package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long userId);

    ItemDto updateItem(ItemDto itemDto, Long userId, Long itemId) throws IllegalAccessException;

    ItemDto findItemById(Long itemId);

    List<ItemDto> findOwnerItems(Long userId);

    List<ItemDto> searchAvailableItems(String text);
}
