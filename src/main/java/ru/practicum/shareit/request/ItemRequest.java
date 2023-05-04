package ru.practicum.shareit.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@NoArgsConstructor
public class ItemRequest {

    Long id;

    @NotBlank
    String itemDescription;

    @NotNull
    Long requestorId;   //User.id

    LocalDateTime created;
}
