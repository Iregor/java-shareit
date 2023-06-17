package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingBookerIdDto;

import java.util.List;

@Data
public class ItemWithBookingsDto extends ItemDto {

    private BookingBookerIdDto lastBooking;

    private BookingBookerIdDto nextBooking;

    private List<CommentDto> comments;

    public ItemWithBookingsDto(Long id, String name, String description, Boolean available, Long ownerId, Long requestId) {
        super(id, name, description, available, ownerId, requestId);
    }
}
