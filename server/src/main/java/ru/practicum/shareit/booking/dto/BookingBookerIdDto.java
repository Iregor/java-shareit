package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingBookerIdDto {
    private Long id;
    private Long bookerId;
}
