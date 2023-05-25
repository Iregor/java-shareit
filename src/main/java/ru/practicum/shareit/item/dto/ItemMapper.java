package ru.practicum.shareit.item.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.repository.UserRepositoryJPA;

@Component
@RequiredArgsConstructor
public class ItemMapper {
    private final UserRepositoryJPA userRepository;

    public static ItemDto toDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public static ItemBookingsIdDto toItemBookingsIdDto(Item item) {
        return new ItemBookingsIdDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable());
    }

    public Item toItem(ItemDto dto, Long userId) {
        return Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .owner(userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found.", userId, String.valueOf(Thread.currentThread().getStackTrace()[1]))))
                .build();
    }
}
