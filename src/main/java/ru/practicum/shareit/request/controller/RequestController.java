package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDesc;
import ru.practicum.shareit.request.dto.RequestWithItems;
import ru.practicum.shareit.request.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class RequestController {
    private final RequestService requestService;

    @PostMapping
    RequestWithItems createRequest(@RequestBody @Valid RequestDesc dto, @RequestHeader("X-Sharer-User-Id") long userId) {
        return requestService.createRequest(dto, userId);
    }

    @GetMapping
    List<RequestWithItems> findAllOwnersRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
        return requestService.findAllOwnersRequests(userId);
    }

    @GetMapping("/all")
    List<RequestWithItems> findAllRequests(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "2147483647") @Positive int size) {
        return requestService.findAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    RequestWithItems findRequestById(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long requestId) {
        return requestService.findRequestById(userId, requestId);
    }
}
