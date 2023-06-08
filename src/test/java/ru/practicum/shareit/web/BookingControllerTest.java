package ru.practicum.shareit.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.exceptions.BookingStatusAlreadyApprovedException;
import ru.practicum.shareit.exception.exceptions.UnknownStateException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService bookingServiceMock;

    @Autowired
    private MockMvc mvc;

    @Test
    public void createBooking_succeed() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 0, 0);
        long userId = 1L;
        BookingDto dto = BookingDto.builder()
                .id(1L)
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

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
                .andExpect(jsonPath("$.start").exists())
                .andExpect(jsonPath("$.end").exists());
    }

    @Test
    public void createBooking_invalidDto_exceptionThrown() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2023, 1, 1, 0, 0);
        long userId = 1L;
        BookingDto dto = BookingDto.builder()
                .id(1L)
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        when(bookingServiceMock.createBooking(dto, userId)).thenReturn(dto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(dto))
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateBooking_succeed() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 0, 0);
        long bookingId = 1L;
        long booker = 1L;
        BookingDto dto = BookingDto.builder()
                .id(bookingId)
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        when(bookingServiceMock.updateBooking(bookingId, true, booker)).thenReturn(dto);

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", booker)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.itemId").value(dto.getItemId()))
                .andExpect(jsonPath("$.start").exists())
                .andExpect(jsonPath("$.end").exists());
    }

    @Test
    public void updateBooking_approvedStatus_exceptionThrown() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 0, 0);
        long bookingId = 1L;
        long booker = 1L;
        BookingDto dto = BookingDto.builder()
                .id(bookingId)
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        when(bookingServiceMock.updateBooking(bookingId, true, booker))
                .thenThrow(new BookingStatusAlreadyApprovedException("Booking status has been already approved.", dto.getId(), String.valueOf(Thread.currentThread().getStackTrace()[1])));

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", booker)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.entityId").value(dto.getId()))
                .andExpect(jsonPath("$.message").value("Booking status has been already approved."));
    }

    @Test
    public void findBookingById_succeed() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 0, 0);
        long bookingId = 1L;
        long booker = 1L;
        BookingDto dto = BookingDto.builder()
                .id(bookingId)
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        when(bookingServiceMock.findBookingById(bookingId, booker)).thenReturn(dto);

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", booker)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.itemId").value(dto.getItemId()))
                .andExpect(jsonPath("$.start").exists())
                .andExpect(jsonPath("$.end").exists());
    }

    @Test
    public void findAlBookingsByOwnerIdAndState_succeed() throws Exception {
        long ownerId = 1L;
        String state = "ALL";
        String from = "0";
        String size = "20";
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 0, 0);
        BookingDto dto1 = BookingDto.builder()
                .id(1L)
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        BookingDto dto2 = BookingDto.builder()
                .id(2L)
                .itemId(1L)
                .start(start.plusYears(1))
                .end(end.plusYears(1))
                .build();

        when(bookingServiceMock.findAllBookingsByOwnerIdAndState(ownerId, state, Integer.parseInt(from), Integer.parseInt(size))).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("state", state)
                        .param("from", from)
                        .param("size", size)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto1.getId()))
                .andExpect(jsonPath("$[1].id").value(dto2.getId()));
    }

    @Test
    public void findAlBookingsByOwnerIdAndState_UnknownState_exceptionThrown() throws Exception {
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
                .andExpect(jsonPath("$.time").exists())
                .andExpect(jsonPath("$.error").value("Unknown state: WrongState"));
    }

    @Test
    public void findAllBookingsForOwnerItemsWithState_succeed() throws Exception {
        long ownerId = 1L;
        String state = "ALL";
        String from = "0";
        String size = "20";
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 0, 0);
        BookingDto dto1 = BookingDto.builder()
                .id(1L)
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        BookingDto dto2 = BookingDto.builder()
                .id(2L)
                .itemId(1L)
                .start(start.plusYears(1))
                .end(end.plusYears(1))
                .build();

        when(bookingServiceMock.findAllBookingsForOwnerItemsWithState(ownerId, state, Integer.parseInt(from), Integer.parseInt(size))).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("state", state)
                        .param("from", from)
                        .param("size", size)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto1.getId()))
                .andExpect(jsonPath("$[1].id").value(dto2.getId()));
    }


}
