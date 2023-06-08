package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.RequestDesc;
import ru.practicum.shareit.request.dto.RequestWithItems;
import ru.practicum.shareit.request.model.Request;

import java.util.List;
import java.util.stream.Collectors;

public class RequestMapper {

    public static RequestWithItems toRequestWithItems(Request request, List<Item> items) {
        return RequestWithItems
                .builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList()))
                .build();
    }

    public static RequestWithItems toRequestWithItems(Request request) {
        return RequestWithItems
                .builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .build();
    }
}
