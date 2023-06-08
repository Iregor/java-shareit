package ru.practicum.shareit.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exception.exceptions.IllegalAccessToEntityException;
import ru.practicum.shareit.exception.exceptions.ItemIdNotConsistentException;
import ru.practicum.shareit.exception.exceptions.NoResolvedBookingException;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemService itemServiceMock;

    @Autowired
    private MockMvc mvc;

    @Test
    public void createItem_valid_succeed() throws Exception {
        ItemDto dto = ItemDto.builder().id(1L).name("item1").description("item1_desc").ownerId(1L).available(true).build();
        when(itemServiceMock.createItem(dto, dto.getOwnerId())).thenReturn(dto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", dto.getOwnerId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.available").value(dto.getAvailable()))
                .andExpect(jsonPath("$.description").value(dto.getDescription()));
    }

    @Test
    public void createItem_UserNotFound_exceptionThrown() throws Exception {
        ItemDto dto = ItemDto.builder().id(1L).name("item1").description("item1_desc").ownerId(1L).available(true).build();
        when(itemServiceMock.createItem(dto, dto.getOwnerId())).thenThrow(new EntityNotFoundException("User not found.", 1L, String.valueOf(Thread.currentThread().getStackTrace()[1])));

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", dto.getOwnerId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.message").value("User not found."))
                .andExpect(jsonPath("$.entityId").value(1L));
    }


    @Test
    public void updateItem_valid_succeed() throws Exception {
        ItemDto dto = ItemDto.builder().id(1L).name("item1").description("item1_desc").ownerId(1L).available(true).build();
        when(itemServiceMock.updateItem(dto, dto.getOwnerId(), dto.getId())).thenReturn(dto);

        mvc.perform(patch("/items/{itemId}", dto.getId())
                        .header("X-Sharer-User-Id", dto.getOwnerId())
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.available").value(dto.getAvailable()))
                .andExpect(jsonPath("$.description").value(dto.getDescription()));
    }

    @Test
    public void updateItem_illegalAccess_exceptionThrown() throws Exception {
        ItemDto dto = ItemDto.builder().id(1L).name("item1").description("item1_desc").ownerId(1L).available(true).build();
        long userId = dto.getOwnerId();
        long itemId = dto.getId();
        when(itemServiceMock.updateItem(dto, dto.getOwnerId(), dto.getId())).thenThrow(new IllegalAccessToEntityException(String.format("Fail to grant access user id: %s to item id: %s", userId, itemId), itemId, userId, String.valueOf(Thread.currentThread().getStackTrace()[1])));

        mvc.perform(patch("/items/{itemId}", dto.getId())
                        .header("X-Sharer-User-Id", dto.getOwnerId())
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.entityId").value(itemId))
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    public void updateItem_itemIdNotConsistent_exceptionThrown() throws Exception {
        ItemDto dto = ItemDto.builder().id(1L).name("item1").description("item1_desc").ownerId(1L).available(true).build();
        long userId = dto.getOwnerId();
        long itemId = dto.getId();
        when(itemServiceMock.updateItem(dto, dto.getOwnerId(), dto.getId()))
                .thenThrow(new ItemIdNotConsistentException(LocalDateTime.now() + " : " + Thread.currentThread().getStackTrace()[1], String.format("Fail to validate id consistensy for dto: %s and itemId: %s", dto, itemId), dto, userId, itemId));

        mvc.perform(patch("/items/{itemId}", dto.getId())
                        .header("X-Sharer-User-Id", dto.getOwnerId())
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.itemDto").value(dto))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.headerUserId").value(userId))
                .andExpect(jsonPath("$.pathVarItemId").value(itemId));
    }

    @Test
    public void findItemById_succeed() throws Exception {
        ItemDto dto = ItemDto.builder().id(1L).name("item1").description("item1_desc").ownerId(1L).available(true).build();
        when(itemServiceMock.findItemById(dto.getId(), dto.getOwnerId())).thenReturn(dto);

        mvc.perform(get("/items/{itemId}", dto.getId())
                        .header("X-Sharer-User-Id", dto.getOwnerId())
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.available").value(dto.getAvailable()))
                .andExpect(jsonPath("$.description").value(dto.getDescription()));
    }

    @Test
    public void findOwnerItems_succeed() throws Exception {
        long ownerId = 1L;
        ItemDto dto1 = ItemDto.builder().id(1L).name("item1").description("item1_desc").ownerId(ownerId).available(true).build();
        ItemDto dto2 = ItemDto.builder().id(2L).name("item2").description("item2_desc").ownerId(ownerId).available(true).build();

        when(itemServiceMock.findOwnerItems(any())).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(dto1.getId()))
                .andExpect(jsonPath("$[1].id").value(dto2.getId()));
    }

    @Test
    public void searchAvailableItems_succeed() throws Exception {
        long ownerId = 1L;
        ItemDto dto1 = ItemDto.builder().id(1L).name("item1").description("item1_desc").ownerId(ownerId).available(true).build();
        ItemDto dto2 = ItemDto.builder().id(2L).name("item2").description("item2_desc").ownerId(ownerId).available(true).build();

        when(itemServiceMock.searchAvailableItems(anyString())).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/items/search")
                        .param("text", "stringToFind")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(dto1.getId()))
                .andExpect(jsonPath("$[1].id").value(dto2.getId()));
    }

    @Test
    public void createComment_succeed() throws Exception {
        CommentDto dto = new CommentDto(1L, "comment1", null, null);
        long itemId = 1L;
        long commentatorId = 1L;
        LocalDateTime created = LocalDateTime.of(2023, 6, 7, 0, 0);

        when(itemServiceMock.createComment(dto, itemId, commentatorId)).thenReturn(new CommentDto(1L, "comment1", "user1", created));

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", commentatorId)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.text").value(dto.getText()))
                .andExpect(jsonPath("$.authorName").value("user1"))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    public void createComment_noResolvedBooking_exceptionThrown() throws Exception {
        CommentDto dto = new CommentDto(1L, "comment1", null, null);
        long itemId = 1L;
        long commentatorId = 1L;

        when(itemServiceMock.createComment(dto, itemId, commentatorId))
                .thenThrow(new NoResolvedBookingException("No resolved booking found.", itemId, commentatorId, String.valueOf(Thread.currentThread().getStackTrace()[1])));

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", commentatorId)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.message").value("No resolved booking found."))
                .andExpect(jsonPath("$.itemId").value(itemId))
                .andExpect(jsonPath("$.userId").value(commentatorId));
    }
}