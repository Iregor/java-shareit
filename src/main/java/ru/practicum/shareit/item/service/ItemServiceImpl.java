package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.exceptions.IllegalAccessToItemException;
import ru.practicum.shareit.exception.exceptions.ItemIdNotConsistentException;
import ru.practicum.shareit.exception.exceptions.RepositoryException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        Optional<Item> itemOpt = itemRepository.createItem(itemMapper.toItem(itemDto, userId));
        Item item = itemOpt.orElseThrow(() -> new RepositoryException(LocalDateTime.now() + " : " + Thread.currentThread().getStackTrace()[1] + String.format(" : fail to create item : %s.", itemDto)));
        return itemMapper.toDto(item);
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long userId, Long itemId) throws IllegalAccessException {
        validateUserAccess(itemDto, userId, itemId);
        validateItemIdConsistency(itemDto, userId, itemId);
        Item itemToSave = itemMapper.toItem(itemDto, userId);
        buildItemEntity(itemToSave, itemId);
        itemToSave.setId(itemId);
        Item updatedItem = itemRepository.updateItem(itemToSave).orElseThrow(() -> new RepositoryException(LocalDateTime.now() + " : " + Thread.currentThread().getStackTrace()[1] + String.format(" : fail to update item : %s.", itemToSave)));
        return itemMapper.toDto(updatedItem);
    }

    private void buildItemEntity(Item itemToSave, Long itemId) throws IllegalAccessException {
        Item itemRepo = itemRepository.findItemById(itemId).orElseThrow(() -> new RepositoryException(LocalDateTime.now() + " : " + Thread.currentThread().getStackTrace()[1] + String.format(" : fail to find item by id : %d.", itemId)));
        Class<? extends Item> clss = itemRepo.getClass();
        for (Field field : clss.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.get(itemToSave) == null) {
                field.set(itemToSave, field.get(itemRepo));
            }
        }
    }

    @Override
    public ItemDto findItemById(Long itemId) {
        return itemMapper.toDto(itemRepository.findItemById(itemId).orElseThrow(() -> new RepositoryException(LocalDateTime.now() + " : " + Thread.currentThread().getStackTrace()[1] + String.format(" : fail to find item by id : %d.", itemId))));
    }

    @Override
    public List<ItemDto> findOwnerItems(Long userId) {
        return itemRepository.findOwnerItems(userId).stream().map(itemMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchAvailableItems(String text) {
        return itemRepository.searchAvailableItems(text).stream().map(itemMapper::toDto).collect(Collectors.toList());
    }


    private void validateUserAccess(ItemDto itemDto, Long userId, Long itemId) {
        Item existedItem = itemRepository.findItemById(itemId).orElseThrow(() -> new RepositoryException(LocalDateTime.now() + " : " + Thread.currentThread().getStackTrace()[1] + String.format(" : fail to find item by id : %d.", itemId)));
        if (!existedItem.getOwner().getId().equals(userId)) {
            throw new IllegalAccessToItemException(LocalDateTime.now() + " : " + Thread.currentThread().getStackTrace()[1], String.format("Fail to grant access user id: %s to item id: %s", userId, itemId), itemDto, userId, itemId);
        }
    }

    private void validateItemIdConsistency(ItemDto itemDto, Long userId, Long itemId) {
        if (itemDto.getId() != null && !itemDto.getId().equals(itemId)) {
            throw new ItemIdNotConsistentException(LocalDateTime.now() + " : " + Thread.currentThread().getStackTrace()[1], String.format("Fail to validate id consistensy for dto: %s and itemId: %s", itemDto, itemId), itemDto, userId, itemId);
        }
    }
}
