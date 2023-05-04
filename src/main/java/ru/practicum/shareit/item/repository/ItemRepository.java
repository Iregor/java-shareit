package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Optional<Item> createItem(Item item);

    Optional<Item> updateItem(Item item);

    Optional<Item> findItemById(Long itemId);

    List<Item> findOwnerItems(Long userId);

    List<Item> searchAvailableItems(String text);

    boolean assertItemExists(Long itemId);
}
