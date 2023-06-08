package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> jsonSerializer;

    @Test
    public void testUserDtoSerialization() throws Exception {
        UserDto userDto = new UserDto(1L, "user1", "user@email.ru");

        JsonContent<UserDto> jsonContent = jsonSerializer.write(userDto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo("user1");
        assertThat(jsonContent).extractingJsonPathStringValue("$.email").isEqualTo("user@email.ru");
    }

    @Test
    public void testUserDtoDeserialization() throws Exception {
        UserDto userDto = new UserDto(1L, "user1", "user@email.ru");

        JsonContent<UserDto> jsonDto = jsonSerializer.write(userDto);

        UserDto deserializedDto = jsonSerializer.parseObject(jsonDto.getJson());

        assertThat(deserializedDto)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "user1")
                .hasFieldOrPropertyWithValue("email", "user@email.ru");
    }
}
