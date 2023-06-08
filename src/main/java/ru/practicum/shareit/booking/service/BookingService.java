package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(BookingDto bookingDto, Long userId);

    BookingDto updateBooking(Long bookingId, boolean approved, Long ownerId);

    BookingDto findBookingById(Long bookingId, Long userId);

    List<BookingDto> findAllBookingsByOwnerIdAndState(Long ownerId, String state, int from, int size);

    List<BookingDto> findAllBookingsForOwnerItemsWithState(Long ownerId, String state, int from, int size);

}
