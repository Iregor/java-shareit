package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDesc;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class RequestController {
    private final RequestClient requestClient;

    @PostMapping
    ResponseEntity<Object> createRequest(@RequestBody @Valid RequestDesc dto,
                                         @RequestHeader("X-Sharer-User-Id") @Positive long userId) {
        return requestClient.createRequest(dto, userId);
    }

    @GetMapping
    ResponseEntity<Object> findAllOwnersRequests(@RequestHeader("X-Sharer-User-Id") @Positive long userId) {
        return requestClient.findAllOwnersRequests(userId);
    }

    @GetMapping("/all")
    ResponseEntity<Object> findAllRequests(
            @RequestHeader("X-Sharer-User-Id") @Positive long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {
        return requestClient.findAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    ResponseEntity<Object> findRequestById(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                           @PathVariable @Positive long requestId) {
        return requestClient.findRequestById(userId, requestId);
    }
}
