package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> jsonSerializer;

    @Test
    public void commentDtoJsonSerializationTest() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("comment")
                .authorName("commentAuthor")
                .created(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
                .build();

        JsonContent<CommentDto> jsonContent = jsonSerializer.write(commentDto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.text").isEqualTo("comment");
        assertThat(jsonContent).extractingJsonPathStringValue("$.authorName").isEqualTo("commentAuthor");
        assertThat(jsonContent).extractingJsonPathStringValue("$.created").isEqualTo("2023-01-01T00:00:00");
    }

    @Test
    public void commentDtoDeserializationTest() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("comment")
                .authorName("commentAuthor")
                .created(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
                .build();

        JsonContent<CommentDto> jsonContent = jsonSerializer.write(commentDto);

        CommentDto deserializedDto = jsonSerializer.parseObject(jsonContent.getJson());

        assertThat(deserializedDto)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("text", "comment")
                .hasFieldOrPropertyWithValue("authorName", "commentAuthor")
                .hasFieldOrPropertyWithValue("created", LocalDateTime.of(2023, 1, 1, 0, 0, 0));
    }
}
