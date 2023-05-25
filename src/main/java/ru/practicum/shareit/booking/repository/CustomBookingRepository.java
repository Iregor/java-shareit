package ru.practicum.shareit.booking.repository;

import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface CustomBookingRepository {

    List<Booking> findAllBookingsByOwnerIdAndState(Long bookerId, String statusCondition);

    List<Booking> findAllBookingsForOwnerItemsWithState(Long ownerId, String statusCondition);
}
