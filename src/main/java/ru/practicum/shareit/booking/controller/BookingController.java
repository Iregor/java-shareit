package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.validation.Exist;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@Validated
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    Booking createBooking(@RequestBody @Valid BookingDto bookingDto, @RequestHeader("X-Sharer-User-Id") @Exist("user") Long userId) {
        return bookingService.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    Booking updateBooking(@PathVariable Long bookingId, @RequestParam boolean approved, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.updateBooking(bookingId, approved, userId);
    }

    @GetMapping("/{bookingId}")
    Booking findBookingById(@PathVariable Long bookingId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.findBookingById(bookingId, userId);
    }

    @GetMapping
    List<Booking> findAllBookingsByOwnerIdAndState(@RequestHeader("X-Sharer-User-Id") Long ownerId, @RequestParam(required = false, defaultValue = "ALL") String state) {
        return bookingService.findAllBookingsByOwnerIdAndState(ownerId, state);
    }

    @GetMapping("/owner")
    List<Booking> findAllBookingsForOwnerItemsWithState(@RequestHeader("X-Sharer-User-Id") Long ownerId, @RequestParam(required = false, defaultValue = "ALL") String state) {
        return bookingService.findAllBookingsForOwnerItemsWithState(ownerId, state);
    }
}
