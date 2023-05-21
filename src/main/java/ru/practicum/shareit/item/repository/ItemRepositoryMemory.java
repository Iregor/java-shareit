package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ItemRepositoryMemory implements ItemRepository {
    private Map<Long, Item> items = new HashMap<>();
    private long currentId;

    @Override
    public Optional<Item> createItem(Item item) {
        item.setId(getNewId());
        items.put(item.getId(), item);
        return Optional.of(item);
    }

    @Override
    public Optional<Item> updateItem(Item item) {
        items.put(item.getId(), item);
        return Optional.of(item);
    }

    @Override
    public Optional<Item> findItemById(Long itemId) {
        return Optional.of(items.get(itemId));
    }

    @Override
    public List<Item> findOwnerItems(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .sorted(Comparator.comparingLong(Item::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchAvailableItems(String text) {
        if (text.isBlank() || text.isEmpty()) {
            return new ArrayList<>();
        }
        return items.values().stream()
                .filter(item -> item.getDescription().toUpperCase().contains(text.toUpperCase()))
                .filter(Item::getAvailable)
                .sorted(Comparator.comparingLong(Item::getId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean assertItemExists(Long id) {
        return items.containsKey(id);
    }

    private long getNewId() {
        return ++currentId;
    }
}
