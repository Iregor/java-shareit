package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.RequestDesc;
import ru.practicum.shareit.request.dto.RequestWithItems;

import java.util.List;

public interface RequestService {
    RequestWithItems createRequest(RequestDesc dto, long userId);

    List<RequestWithItems> findAllOwnersRequests(long userId);

    List<RequestWithItems> findAllRequests(long userId, int from, int size);

    RequestWithItems findRequestById(long userId, long requestId);
}
