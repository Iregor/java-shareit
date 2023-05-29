package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
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
    BookingDto createBooking(@RequestBody @Valid BookingDto bookingDto, @RequestHeader("X-Sharer-User-Id") @Exist("user") Long userId) {
        return bookingService.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    BookingDto updateBooking(@PathVariable Long bookingId, @RequestParam boolean approved, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.updateBooking(bookingId, approved, userId);
    }

    @GetMapping("/{bookingId}")
    BookingDto findBookingById(@PathVariable Long bookingId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.findBookingById(bookingId, userId);
    }

    @GetMapping
    List<BookingDto> findAllBookingsByOwnerIdAndState(@RequestHeader("X-Sharer-User-Id") Long ownerId, @RequestParam(required = false, defaultValue = "ALL") String state) {
        return bookingService.findAllBookingsByOwnerIdAndState(ownerId, state);
    }

    @GetMapping("/owner")
    List<BookingDto> findAllBookingsForOwnerItemsWithState(@RequestHeader("X-Sharer-User-Id") Long ownerId, @RequestParam(required = false, defaultValue = "ALL") String state) {
        return bookingService.findAllBookingsForOwnerItemsWithState(ownerId, state);
    }
}
