package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingBookerIdDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemWithBookingsDtoTest {

    @Autowired
    private JacksonTester<ItemWithBookingsDto> jsonSerializer;

    @Test
    public void ItemWithBookingsDtoSerializationTest() throws Exception {
        ItemWithBookingsDto dto = new ItemWithBookingsDto(1L, "item1", "item1_desc", true, 2L, 3L);
        dto.setLastBooking(new BookingBookerIdDto(4L, 4L));
        dto.setNextBooking(new BookingBookerIdDto(5L, 5L));
        dto.setComments(List.of(new CommentDto(1L, "comment1", "author1", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))));

        JsonContent<ItemWithBookingsDto> jsonContent = jsonSerializer.write(dto);

        System.out.println(jsonContent.getJson());

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo("item1");
        assertThat(jsonContent).extractingJsonPathStringValue("$.description").isEqualTo("item1_desc");
        assertThat(jsonContent).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.ownerId").isEqualTo(2);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.requestId").isEqualTo(3);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(4);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(4);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(5);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.nextBooking.bookerId").isEqualTo(5);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.comments[0].id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.comments[0].text").isEqualTo("comment1");
        assertThat(jsonContent).extractingJsonPathStringValue("$.comments[0].authorName").isEqualTo("author1");
    }
}
