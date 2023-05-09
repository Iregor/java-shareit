package ru.practicum.shareit.item.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.exceptions.RepositoryException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ItemMapper {
    private final UserRepository userRepository;

    public ItemDto toDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public Item toItem(ItemDto dto, Long userId) {
        return Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .owner(userRepository.findUserById(userId).orElseThrow(() -> new RepositoryException(LocalDateTime.now() + " : " + Thread.currentThread().getStackTrace()[1] + String.format(" : fail to find user by id : %d.", userId))))
                .build();
    }
}
