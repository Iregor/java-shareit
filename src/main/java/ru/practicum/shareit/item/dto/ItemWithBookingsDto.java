package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingBookerIdDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ItemWithBookingsDto extends ItemDto {

    private BookingBookerIdDto lastBooking;

    private BookingBookerIdDto nextBooking;

    private List<CommentDto> comments;

    public ItemWithBookingsDto(Long id, @NotBlank String name, @NotBlank String description, @NotNull Boolean available, Long ownerId, Long requestId) {
        super(id, name, description, available, ownerId, requestId);
    }
}
