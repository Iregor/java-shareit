package ru.practicum.shareit.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemServiceMock;

    @Autowired
    private MockMvc mvc;

    private ItemDto dto, dto1, dto2;
    private CommentDto commentDto;

    @BeforeEach
    public void createEntities() {
        dto = ItemDto.builder().id(1L).name("item1").description("item1_desc").ownerId(1L).available(true).build();
        dto1 = ItemDto.builder().id(1L).name("item1").description("item1_desc").ownerId(1L).available(true).build();
        dto2 = ItemDto.builder().id(2L).name("item2").description("item2_desc").ownerId(1L).available(true).build();
        commentDto = new CommentDto(1L, "comment1", null, null);
    }

    @Test
    public void createItem_valid_succeed() throws Exception {
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
                .andExpect(jsonPath("$.description").value(dto.getDescription()))
                .andExpect(jsonPath("$.ownerId").value(dto.getOwnerId()));
    }

    @Test
    public void createItem_UserNotFound_exceptionThrown() throws Exception {
        when(itemServiceMock.createItem(dto, dto.getOwnerId())).thenThrow(new EntityNotFoundException("User not found.", 1L, String.valueOf(Thread.currentThread().getStackTrace()[1])));

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", dto.getOwnerId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntityNotFoundException))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.message").value("User not found."))
                .andExpect(jsonPath("$.entityId").value(1L));
    }


    @Test
    public void updateItem_valid_succeed() throws Exception {
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
                .andExpect(jsonPath("$.description").value(dto.getDescription()))
                .andExpect(jsonPath("$.ownerId").value(dto.getOwnerId()));
    }

    @Test
    public void updateItem_illegalAccess_exceptionThrown() throws Exception {
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
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalAccessToEntityException))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.entityId").value(itemId))
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    public void updateItem_itemIdNotConsistent_exceptionThrown() throws Exception {
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
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ItemIdNotConsistentException))
                .andExpect(jsonPath("$.itemDto").value(dto))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.headerUserId").value(userId))
                .andExpect(jsonPath("$.pathVarItemId").value(itemId));
    }

    @Test
    public void findItemById_succeed() throws Exception {
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
                .andExpect(jsonPath("$.description").value(dto.getDescription()))
                .andExpect(jsonPath("$.ownerId").value(dto.getOwnerId()));
    }

    @Test
    public void findOwnerItems_succeed() throws Exception {
        long ownerId = 1L;

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
        when(itemServiceMock.searchAvailableItems(anyString())).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/items/search")
                        .param("text", "stringToFind")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(dto1.getId()))
                .andExpect(jsonPath("$[0].name").value(dto1.getName()))
                .andExpect(jsonPath("$[0].description").value(dto1.getDescription()))
                .andExpect(jsonPath("$[0].available").value(dto1.getAvailable()))
                .andExpect(jsonPath("$[0].ownerId").value(dto1.getOwnerId()))
                .andExpect(jsonPath("$[1].id").value(dto2.getId()))
                .andExpect(jsonPath("$[1].name").value(dto2.getName()))
                .andExpect(jsonPath("$[1].description").value(dto2.getDescription()))
                .andExpect(jsonPath("$[1].available").value(dto2.getAvailable()))
                .andExpect(jsonPath("$[1].ownerId").value(dto2.getOwnerId()));
    }

    @Test
    public void createComment_succeed() throws Exception {
        long itemId = 1L;
        long commentatorId = 1L;
        LocalDateTime created = LocalDateTime.of(2023, 6, 7, 0, 0);

        when(itemServiceMock.createComment(commentDto, itemId, commentatorId)).thenReturn(new CommentDto(1L, "comment1", "user1", created));

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", commentatorId)
                        .content(mapper.writeValueAsString(commentDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentDto.getId()))
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andExpect(jsonPath("$.authorName").value("user1"))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    public void createComment_noResolvedBooking_exceptionThrown() throws Exception {
        long itemId = 1L;
        long commentatorId = 1L;

        when(itemServiceMock.createComment(commentDto, itemId, commentatorId))
                .thenThrow(new NoResolvedBookingException("No resolved booking found.", itemId, commentatorId, String.valueOf(Thread.currentThread().getStackTrace()[1])));

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", commentatorId)
                        .content(mapper.writeValueAsString(commentDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NoResolvedBookingException))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.message").value("No resolved booking found."))
                .andExpect(jsonPath("$.itemId").value(itemId))
                .andExpect(jsonPath("$.userId").value(commentatorId));
    }
}