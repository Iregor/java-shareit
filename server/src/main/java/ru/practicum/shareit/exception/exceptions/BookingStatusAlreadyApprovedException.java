package ru.practicum.shareit.exception.exceptions;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingStatusAlreadyApprovedException extends RuntimeException {
    private Long bookingId;
    private String backInfo;
    private LocalDateTime time;

    public BookingStatusAlreadyApprovedException(String message, Long bookingId, String backInfo) {
        super(message);
        this.bookingId = bookingId;
        this.backInfo = backInfo;
    }
}
