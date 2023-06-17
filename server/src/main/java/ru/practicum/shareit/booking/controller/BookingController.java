package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@Validated
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(@RequestBody BookingDto bookingDto, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateBooking(@PathVariable Long bookingId, @RequestParam boolean approved, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.updateBooking(bookingId, approved, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findBookingById(@PathVariable Long bookingId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.findBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> findAllBookingsByOwnerIdAndState(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam String state,
            @RequestParam int from,
            @RequestParam int size) {
        return bookingService.findAllBookingsByOwnerIdAndState(ownerId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> findAllBookingsForOwnerItemsWithState(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam String state,
            @RequestParam int from,
            @RequestParam int size) {
        return bookingService.findAllBookingsForOwnerItemsWithState(ownerId, state, from, size);
    }
}
