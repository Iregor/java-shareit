package ru.practicum.shareit.exception.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UnknownStateResponse {
    private LocalDateTime time;
    private String error;
}
