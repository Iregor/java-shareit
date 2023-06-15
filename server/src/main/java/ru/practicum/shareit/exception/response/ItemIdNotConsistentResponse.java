package ru.practicum.shareit.exception.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ItemIdNotConsistentResponse {
    private String message;
    private ItemDto itemDto;
    private Long headerUserId;
    private Long pathVarItemId;
}