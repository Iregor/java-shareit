package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> jsonSerializer;

    @Test
    public void ItemDtoJsonSerializationTest() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("item1")
                .description("item1_desc")
                .available(true)
                .ownerId(2L)
                .requestId(3L)
                .build();

        JsonContent<ItemDto> jsonContent = jsonSerializer.write(itemDto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo("item1");
        assertThat(jsonContent).extractingJsonPathStringValue("$.description").isEqualTo("item1_desc");
        assertThat(jsonContent).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.ownerId").isEqualTo(2);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.requestId").isEqualTo(3);
    }

    @Test
    public void ItemDtoDeserializationTest() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("item1")
                .description("item1_desc")
                .available(true)
                .ownerId(2L)
                .requestId(3L)
                .build();

        JsonContent<ItemDto> jsonContent = jsonSerializer.write(itemDto);

        ItemDto deserializedDto = jsonSerializer.parseObject(jsonContent.getJson());

        assertThat(deserializedDto)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "item1")
                .hasFieldOrPropertyWithValue("description", "item1_desc")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("ownerId", 2L)
                .hasFieldOrPropertyWithValue("requestId", 3L);
    }
}