package ru.practicum.shareit.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.controller.RequestController;
import ru.practicum.shareit.request.dto.RequestDesc;
import ru.practicum.shareit.request.dto.RequestWithItems;
import ru.practicum.shareit.request.service.RequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
public class RequestControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    RequestService requestServiceMock;

    @Autowired
    private MockMvc mvc;

    @Test
    public void createRequest_succeed() throws Exception {
        RequestDesc dto = new RequestDesc("request1");
        long requestorId = 1L;

        when(requestServiceMock.createRequest(dto, requestorId))
                .thenReturn(new RequestWithItems(1L, "request1", LocalDateTime.now(), null));

        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", requestorId)
                        .content(mapper.writeValueAsString(dto))
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("request1"))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    public void createRequest_invalidDto_exceptionThrown() throws Exception {
        RequestDesc dto = new RequestDesc("    ");
        long requestorId = 1L;

        when(requestServiceMock.createRequest(dto, requestorId))
                .thenReturn(new RequestWithItems(1L, "request1", LocalDateTime.now(), null));

        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", requestorId)
                        .content(mapper.writeValueAsString(dto))
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void findAllOwnersRequests_succeed() throws Exception {
        long requestorId = 1L;

        RequestWithItems dto1 = new RequestWithItems(1L, "dto1", LocalDateTime.now(), null);
        RequestWithItems dto2 = new RequestWithItems(2L, "dto2", LocalDateTime.now(), null);

        when(requestServiceMock.findAllOwnersRequests(requestorId)).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", requestorId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto1.getId()))
                .andExpect(jsonPath("$[0].description").value(dto1.getDescription()))
                .andExpect(jsonPath("$[0].created").exists())
                .andExpect(jsonPath("$[1].id").value(dto2.getId()))
                .andExpect(jsonPath("$[1].created").exists())
                .andExpect(jsonPath("$[1].description").value(dto2.getDescription()));
    }

    @Test
    public void findAllRequests_succeed() throws Exception {
        long requestorId = 1L;
        String from = "0";
        String size = "20";

        RequestWithItems dto1 = new RequestWithItems(1L, "dto1", LocalDateTime.now(), null);
        RequestWithItems dto2 = new RequestWithItems(2L, "dto2", LocalDateTime.now(), null);

        when(requestServiceMock.findAllRequests(requestorId, Integer.parseInt(from), Integer.parseInt(size))).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", requestorId)
                        .param("from", from)
                        .param("size", size)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto1.getId()))
                .andExpect(jsonPath("$[0].description").value(dto1.getDescription()))
                .andExpect(jsonPath("$[0].created").exists())
                .andExpect(jsonPath("$[1].id").value(dto2.getId()))
                .andExpect(jsonPath("$[1].created").exists())
                .andExpect(jsonPath("$[1].description").value(dto2.getDescription()));
    }

    @Test
    public void findRequestByid_succeed() throws Exception {
        long requestId = 1L;
        long requestorId = 1L;

        when(requestServiceMock.findRequestById(requestorId, requestId)).thenReturn(new RequestWithItems(1L, "request1", LocalDateTime.now(), null));

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", requestorId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("request1"))
                .andExpect(jsonPath("$.created").exists());
    }
}
