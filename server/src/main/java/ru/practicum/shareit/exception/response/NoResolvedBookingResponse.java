package ru.practicum.shareit.exception.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoResolvedBookingResponse {
    private LocalDateTime time;
    private String message;
    private Long itemId;
    private Long userId;
}
