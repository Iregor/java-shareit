package ru.practicum.shareit.exception.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EntityNotFoundResponse {
    private LocalDateTime time;
    private String message;
    private Long entityId;
}
