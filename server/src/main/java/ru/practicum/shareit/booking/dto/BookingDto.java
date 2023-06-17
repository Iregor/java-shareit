package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class BookingDto {

    private Long id;

    private Long itemId;

    private Item item;

    private Booker booker;

    private LocalDateTime start;

    private LocalDateTime end;

    private BookingStatus status;

    @Data
    @RequiredArgsConstructor
    public static class Booker {
        private final long id;
        private final String name;
    }

    @Data
    public static class Item {
        private final long id;
        private final String name;
    }
}
