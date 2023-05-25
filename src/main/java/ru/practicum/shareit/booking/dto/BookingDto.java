package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.validation.BookingDateConsistency;
import ru.practicum.shareit.validation.Exist;
import ru.practicum.shareit.validation.ItemAvailable;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@BookingDateConsistency
public class BookingDto {

    private Long id;

    @NotNull
    @Exist("item")
    @ItemAvailable
    private Long itemId;

    private Long bookerId;

    @NotNull
    @Future
    private LocalDateTime start;

    @NotNull
    @Future
    private LocalDateTime end;

    private BookingStatus status;
}
