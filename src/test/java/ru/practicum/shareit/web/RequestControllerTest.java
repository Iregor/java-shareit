package ru.practicum.shareit.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.request.controller.RequestController;
import ru.practicum.shareit.request.dto.RequestDesc;
import ru.practicum.shareit.request.dto.RequestWithItems;
import ru.practicum.shareit.request.service.RequestService;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
public class RequestControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private RequestService requestServiceMock;
    private RequestDesc dto;
    private RequestWithItems dto1, dto2;
    private long requestorId;
    private long requestId;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");


    @Autowired
    private MockMvc mvc;

    @BeforeEach
    public void createEntities() {
        dto = new RequestDesc("request1");
        requestorId = 1L;
        requestId = 1L;

        dto1 = new RequestWithItems(1L, "dto1", LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES), null);
        dto2 = new RequestWithItems(2L, "dto2", LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES), null);
    }

    @Test
    public void createRequest_succeed() throws Exception {
        when(requestServiceMock.createRequest(dto, requestorId))
                .thenReturn(new RequestWithItems(1L, "request1", LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES), null));

        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", requestorId)
                        .content(mapper.writeValueAsString(dto))
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("request1"))
                .andExpect(jsonPath("$.created").value(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).format(formatter)));
    }

    @Test
    public void createRequest_blankDescription_exceptionThrown() throws Exception {
        String blankDescription = "   ";
        dto.setDescription(blankDescription);
        when(requestServiceMock.createRequest(dto, requestorId))
                .thenReturn(new RequestWithItems(1L, "request1", LocalDateTime.now(), null));

        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", requestorId)
                        .content(mapper.writeValueAsString(dto))
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    public void findAllOwnersRequests_succeed() throws Exception {
        when(requestServiceMock.findAllOwnersRequests(requestorId)).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", requestorId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(dto1.getId()))
                .andExpect(jsonPath("$[0].description").value(dto1.getDescription()))
                .andExpect(jsonPath("$[0].created").value(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).format(formatter)))
                .andExpect(jsonPath("$[1].id").value(dto2.getId()))
                .andExpect(jsonPath("$[1].created").value(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).format(formatter)))
                .andExpect(jsonPath("$[1].description").value(dto2.getDescription()));
    }

    @Test
    public void findAllRequests_succeed() throws Exception {
        String from = "0";
        String size = "20";
        when(requestServiceMock.findAllRequests(requestorId, Integer.parseInt(from), Integer.parseInt(size))).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", requestorId)
                        .param("from", from)
                        .param("size", size)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(dto1.getId()))
                .andExpect(jsonPath("$[0].description").value(dto1.getDescription()))
                .andExpect(jsonPath("$[0].created").value(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).format(formatter)))
                .andExpect(jsonPath("$[1].id").value(dto2.getId()))
                .andExpect(jsonPath("$[1].created").value(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).format(formatter)))
                .andExpect(jsonPath("$[1].description").value(dto2.getDescription()));
    }

    @Test
    public void findAllRequests_negativePageableFrom_succeed() throws Exception {
        String from = "-1";
        String size = "20";
        when(requestServiceMock.findAllRequests(requestorId, Integer.parseInt(from), Integer.parseInt(size))).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", requestorId)
                        .param("from", from)
                        .param("size", size)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException));
    }

    @Test
    public void findAllRequests_negativePageableSize_succeed() throws Exception {
        String from = "0";
        String size = "-20";
        when(requestServiceMock.findAllRequests(requestorId, Integer.parseInt(from), Integer.parseInt(size))).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", requestorId)
                        .param("from", from)
                        .param("size", size)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException));
    }

    @Test
    public void findRequestById_succeed() throws Exception {
        when(requestServiceMock.findRequestById(requestorId, requestId)).thenReturn(new RequestWithItems(1L, "request1", LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES), null));

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", requestorId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("request1"))
                .andExpect(jsonPath("$.created").value(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).format(formatter)));
    }
}