package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.Collections;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestBody @Valid ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemClient.createItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") @Positive Long userId, @PathVariable Long itemId) throws IllegalAccessException {
        return itemClient.updateItem(itemDto, userId, itemId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findItemById(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemClient.findItemById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> findOwnerItems(@RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemClient.findOwnerItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchAvailableItems(@RequestParam String text, @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        if (text.isBlank() || text.isEmpty()) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }
        return itemClient.searchAvailableItems(text, userId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestBody @Valid CommentDto commentDto, @PathVariable @Positive Long itemId, @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemClient.createComment(commentDto, itemId, userId);
    }

    public void assertValidText(String text) {

    }
}