package ru.practicum.shareit.feedback.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class FeedBack {

    Long id;

    @Positive
    Long itemId;

    @Positive
    Long userId;

    @NotBlank
    private String content;

    @NotNull
    private Boolean useful;
}
