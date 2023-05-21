package ru.practicum.shareit.feedback.model;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class FeedBack {

    Long id;

    @NotNull
    Item item;

    @NotNull
    User user;

    @NotBlank
    private String content;

    @NotNull
    private Boolean useful;
}
