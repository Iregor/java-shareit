package ru.practicum.shareit.booking.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.initializer.JpaProxyInitializer;
import ru.practicum.shareit.item.repository.ItemRepositoryJPA;
import ru.practicum.shareit.user.repository.UserRepositoryJPA;

@Component
@RequiredArgsConstructor
public class BookingMapper {
    private final ItemRepositoryJPA itemRepository;
    private final UserRepositoryJPA userRepository;

    public Booking toBooking(BookingDto dto) {
        return Booking.builder()
                .id(dto.getId())
                .item(itemRepository.findById(dto.getItemId()).get())
                .booker(userRepository.findById(dto.getBookerId()).get())
                .start(dto.getStart())
                .end(dto.getEnd())
                .status(dto.getStatus())
                .build();
    }

    public Booking toBooking(BookingDto dto, Long bookerId) {
        return Booking.builder()
                .id(dto.getId())
                .item(itemRepository.findById(dto.getItemId()).get())
                .booker(userRepository.findById(bookerId).get())
                .start(dto.getStart())
                .end(dto.getEnd())
                .status(dto.getStatus())
                .build();
    }

    public BookingDto toDto(Booking entity) {
        return BookingDto.builder()
                .id(entity.getId())
                .bookerId(entity.getId())
                .itemId(entity.getItem().getId())
                .bookerId(entity.getBooker().getId())
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