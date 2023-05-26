package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.initializer.JpaProxyInitializer;

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
//                .itemId(entity.getItem().getId())
//                .bookerId(entity.getBooker().getId())
                .start(entity.getStart())
                .end(entity.getEnd())
                .status(entity.getStatus())
                .build();
    }

    public static BookingBookerIdDto toBookingBookerIdDto(Booking entity) {
        return entity != null ? new BookingBookerIdDto(entity.getId(), entity.getBooker().getId()) : null;
    }

    public static Booking initializeBooking(Booking booking) {
        booking.setItem(JpaProxyInitializer.initialize(booking.getItem()));
        booking.setBooker(JpaProxyInitializer.initialize(booking.getBooker()));
        booking.getItem().setOwner(JpaProxyInitializer.initialize(booking.getItem().getOwner()));
        return booking;
    }
}
