package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.Booking;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingMapper {

    public static Booking toBooking(BookingDto dto) {
        return Booking.builder()
                .id(dto.getId())
                .start(dto.getStart())
                .end(dto.getEnd())
                .status(dto.getStatus())
                .build();
    }

    public static BookingDto toDto(Booking entity) {
        return BookingDto.builder()
                .id(entity.getId())
                .booker(new BookingDto.Booker(entity.getBooker().getId(), entity.getBooker().getName()))
                .item(new BookingDto.Item(entity.getItem().getId(), entity.getItem().getName()))
                .start(entity.getStart())
                .end(entity.getEnd())
                .status(entity.getStatus())
                .build();
    }

    public static BookingBookerIdDto toBookingBookerIdDto(Booking entity) {
        return entity != null ? new BookingBookerIdDto(entity.getId(), entity.getBooker().getId()) : null;
    }
}
