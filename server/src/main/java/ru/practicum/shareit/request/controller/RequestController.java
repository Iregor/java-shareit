package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDesc;
import ru.practicum.shareit.request.dto.RequestWithItems;
import ru.practicum.shareit.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class RequestController {
    private final RequestService requestService;

    @PostMapping
    RequestWithItems createRequest(@RequestBody RequestDesc dto, @RequestHeader("X-Sharer-User-Id") long userId) {
        return requestService.createRequest(dto, userId);
    }

    @GetMapping
    List<RequestWithItems> findAllOwnersRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
        return requestService.findAllOwnersRequests(userId);
    }

    @GetMapping("/all")
    List<RequestWithItems> findAllRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam int from,
            @RequestParam int size) {
        return requestService.findAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    RequestWithItems findRequestById(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long requestId) {
        return requestService.findRequestById(userId, requestId);
    }
}
