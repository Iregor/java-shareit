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
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.exceptions.BookingStatusAlreadyApprovedException;
import ru.practicum.shareit.exception.exceptions.UnknownStateException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingServiceMock;

    @Autowired
    private MockMvc mvc;

    private LocalDateTime start, end;
    private long userId, bookingId, bookerId;
    private BookingDto dto, dto1, dto2;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @BeforeEach
    public void createEntities() {
        start = LocalDateTime.of(2024, 1, 1, 0, 0);
        end = LocalDateTime.of(2025, 1, 1, 0, 0);
        userId = 1L;
        bookingId = 1L;
        bookerId = 1L;

        dto = BookingDto.builder()
                .id(1L)
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        dto1 = BookingDto.builder()
                .id(1L)
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        dto2 = BookingDto.builder()
                .id(2L)
                .itemId(1L)
                .start(start.plusYears(1))
                .end(end.plusYears(1))
                .build();
    }

    @Test
    public void createBooking_succeed() throws Exception {
        when(bookingServiceMock.createBooking(dto, userId)).thenReturn(dto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(dto))
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.itemId").value(dto.getItemId()))
                .andExpect(jsonPath("$.start").value(start.format(formatter)))
                .andExpect(jsonPath("$.end").value(end.format(formatter)));
    }

    @Test
    public void createBooking_endBeforeStart_exceptionThrown() throws Exception {
        start = LocalDateTime.of(2024, 1, 1, 0, 0);
        end = LocalDateTime.of(2023, 1, 1, 0, 0);
        dto.setStart(start);
        dto.setEnd(end);

        when(bookingServiceMock.createBooking(dto, userId)).thenReturn(dto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(dto))
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    public void createBooking_nullItemId_exceptionThrown() throws Exception {
        dto.setItemId(null);

        when(bookingServiceMock.createBooking(dto, userId)).thenReturn(dto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(dto))
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    public void createBooking_startInThePast_exceptionThrown() throws Exception {
        start = LocalDateTime.of(2022, 1, 1, 0, 0, 0);
        dto.setStart(start);

        when(bookingServiceMock.createBooking(dto, userId)).thenReturn(dto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(dto))
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    public void createBooking_nullStart_exceptionThrown() throws Exception {
        dto.setStart(null);

        when(bookingServiceMock.createBooking(dto, userId)).thenReturn(dto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(dto))
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    public void createBooking_nullEnd_exceptionThrown() throws Exception {
        dto.setEnd(null);

        when(bookingServiceMock.createBooking(dto, userId)).thenReturn(dto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(dto))
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }

    @Test
    public void updateBooking_succeed() throws Exception {
        when(bookingServiceMock.updateBooking(bookingId, true, bookerId)).thenReturn(dto);

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", bookerId)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.itemId").value(dto.getItemId()))
                .andExpect(jsonPath("$.start").value(start.format(formatter)))
                .andExpect(jsonPath("$.end").value(end.format(formatter)));
    }

    @Test
    public void updateBooking_approvedStatus_exceptionThrown() throws Exception {
        when(bookingServiceMock.updateBooking(bookingId, true, bookerId))
                .thenThrow(new BookingStatusAlreadyApprovedException("Booking status has been already approved.", dto.getId(), String.valueOf(Thread.currentThread().getStackTrace()[1])));

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", bookerId)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BookingStatusAlreadyApprovedException))
                .andExpect(jsonPath("$.entityId").value(dto.getId()))
                .andExpect(jsonPath("$.message").value("Booking status has been already approved."));
    }

    @Test
    public void findBookingById_succeed() throws Exception {
        when(bookingServiceMock.findBookingById(bookingId, bookerId)).thenReturn(dto);

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", bookerId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.itemId").value(dto.getItemId()))
                .andExpect(jsonPath("$.start").value(start.format(formatter)))
                .andExpect(jsonPath("$.end").value(end.format(formatter)));
    }

    @Test
    public void findAlBookingsByOwnerIdAndState_succeed() throws Exception {
        long ownerId = 1L;
        String state = "ALL";
        String from = "0";
        String size = "20";

        when(bookingServiceMock.findAllBookingsByOwnerIdAndState(ownerId, state, Integer.parseInt(from), Integer.parseInt(size))).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("state", state)
                        .param("from", from)
                        .param("size", size)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(dto1.getId()))
                .andExpect(jsonPath("$[0].itemId").value(dto1.getItemId()))
                .andExpect(jsonPath("$[0].start").value(dto1.getStart().format(formatter)))
                .andExpect(jsonPath("$[0].end").value(dto1.getEnd().format(formatter)))
                .andExpect(jsonPath("$[1].id").value(dto2.getId()))
                .andExpect(jsonPath("$[1].itemId").value(dto2.getItemId()))
                .andExpect(jsonPath("$[1].start").value(dto2.getStart().format(formatter)))
                .andExpect(jsonPath("$[1].end").value(dto2.getEnd().format(formatter)));
    }

    @Test
    public void findAllBookingsByOwnerIdAndState_UnknownState_exceptionThrown() throws Exception {
        long ownerId = 1L;
        String state = "WrongState";
        String from = "0";
        String size = "20";

        when(bookingServiceMock.findAllBookingsByOwnerIdAndState(ownerId, state, Integer.parseInt(from), Integer.parseInt(size)))
                .thenThrow(new UnknownStateException(String.format("Unknown state: %s", state), state, String.valueOf(Thread.currentThread().getStackTrace()[1])));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("state", state)
                        .param("from", from)
                        .param("size", size)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UnknownStateException))
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.error").value("Unknown state: WrongState"));
    }

    @Test
    public void findAllBookingsForOwnerItemsWithState_succeed() throws Exception {
        long ownerId = 1L;
        String state = "ALL";
        String from = "0";
        String size = "20";

        when(bookingServiceMock.findAllBookingsForOwnerItemsWithState(ownerId, state, Integer.parseInt(from), Integer.parseInt(size))).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("state", state)
                        .param("from", from)
                        .param("size", size)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(dto1.getId()))
                .andExpect(jsonPath("$[0].itemId").value(dto1.getItemId()))
                .andExpect(jsonPath("$[0].start").value(dto1.getStart().format(formatter)))
                .andExpect(jsonPath("$[0].end").value(dto1.getEnd().format(formatter)))
                .andExpect(jsonPath("$[1].id").value(dto2.getId()))
                .andExpect(jsonPath("$[1].itemId").value(dto2.getItemId()))
                .andExpect(jsonPath("$[1].start").value(dto2.getStart().format(formatter)))
                .andExpect(jsonPath("$[1].end").value(dto2.getEnd().format(formatter)));
    }
}
