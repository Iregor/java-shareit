package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {
    Booking createBooking(BookingDto bookingDto, Long userId);

    Booking updateBooking(Long bookingId, boolean approved, Long ownerId);

    Booking findBookingById(Long bookingId, Long userId);

    List<Booking> findAllBookingsByOwnerIdAndState(Long ownerId, String state);

    List<Booking> findAllBookingsForOwnerItemsWithState(Long ownerId, String state);

}
