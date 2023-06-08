package ru.practicum.shareit.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    UserService userService;

    @Autowired
    Validator validator;

    @Autowired
    private MockMvc mvc;

    @Test
    public void createUser_valid_succeed() throws Exception {
        User user = new User(null, "user1_name", "user1@email.ru");
        UserDto dto = UserMapper.toDto(user);
        dto.setId(1L);

        Mockito.when(userService.createUser(any())).thenReturn(dto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(user))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("email").value(dto.getEmail()));
        Mockito.verify(userService).createUser(Mockito.any());
    }

    @Test
    public void createUser_invalidUser_exceptionThrown() throws Exception {
        User user = new User(null, "   ", "user1email.ru");
        UserDto dto = UserMapper.toDto(user);
        dto.setId(1L);

        Mockito.when(userService.createUser(any())).thenReturn(dto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(user))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void findUserById_existingUser_succeed() throws Exception {
        User user = new User(null, "user1_name", "user1@email.ru");
        UserDto dto = UserMapper.toDto(user);
        dto.setId(1L);

        Mockito.when(userService.findUserById(any())).thenReturn(dto);

        mvc.perform(get("/users/{userId}", dto.getId())
                        .content(mapper.writeValueAsString(user))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("email").value(dto.getEmail()));
        Mockito.verify(userService).findUserById(Mockito.any());
    }

    @Test
    public void findAllUsers_succeed() throws Exception {
        UserDto dto1 = UserDto.builder().id(1L).name("name1").email("name1@email.ru").build();
        UserDto dto2 = UserDto.builder().id(2L).name("name2").email("name2@email.ru").build();
        Mockito.when(userService.findAllUsers()).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto1.getId()))
                .andExpect(jsonPath("$[0].name").value(dto1.getName()))
                .andExpect(jsonPath("$[0].email").value(dto1.getEmail()))
                .andExpect(jsonPath("$[1].id").value(dto2.getId()))
                .andExpect(jsonPath("$[1].name").value(dto2.getName()))
                .andExpect(jsonPath("$[1].email").value(dto2.getEmail()));
    }

    @Test
    public void patchUser_succeed() throws Exception {
        User user = new User(null, "user1_name", "user1@email.ru");
        UserDto dto = UserMapper.toDto(user);
        dto.setId(1L);

        Mockito.when(userService.patchUser(any(), any())).thenReturn(dto);

        mvc.perform(patch("/users/{userId}", dto.getId())
                        .content(mapper.writeValueAsString(user))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("email").value(dto.getEmail()));
        Mockito.verify(userService).patchUser(any(), any());
    }

    @Test
    public void patchUser_validationException_exceptionThrown() throws Exception {
        User user = new User(null, "   ", "user1email.ru");
        UserDto dto = UserMapper.toDto(user);
        dto.setId(1L);

        Mockito.when(userService.patchUser(any(), any()))
                .thenThrow(new ConstraintViolationException(validator.validate(user)));

        mvc.perform(patch("/users/{userId}", dto.getId())
                        .content(mapper.writeValueAsString(user))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteUser_succeed() throws Exception {
        mvc.perform(delete("/users/{userId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Mockito.verify(userService).deleteUserById(any());
    }
}