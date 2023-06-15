package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class BookingServiceImplTest {
    private final ItemServiceImpl itemService;
    private final UserServiceImpl userService;
    private final BookingServiceImpl bookingService;
    User owner;
    User booker;
    ItemDto itemDto;
    BookingDto bookingDto, bookingDto1, bookingDto2;
    long ownerId, bookerId, itemId;

    @BeforeEach
    public void createEntities() {
        owner = new User(null, "owner", "owner@email.ru");
        booker = new User(null, "booker", "booker@email.ru");
        itemDto = ItemDto.builder().name("item1_name").description("item1_desc").available(true).build();
        ownerId = userService.createUser(owner).getId();
        bookerId = userService.createUser(booker).getId();
        itemId = itemService.createItem(itemDto, ownerId).getId();
        bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusSeconds(10))
                .end(LocalDateTime.now().plusSeconds(20))
                .build();
        bookingDto1 = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusSeconds(10))
                .end(LocalDateTime.now().plusSeconds(10))
                .build();
        bookingDto2 = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusSeconds(5))
                .end(LocalDateTime.now().plusSeconds(5))
                .build();
    }

    @Test
    @Transactional
    public void createBooking_succeed() {
        assertThat(bookingDto = bookingService.createBooking(bookingDto, bookerId))
                .hasFieldOrPropertyWithValue("id", bookingDto.getId())
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING)
                .hasFieldOrPropertyWithValue("start", bookingDto.getStart())
                .hasFieldOrPropertyWithValue("end", bookingDto.getEnd());
        assertThat(bookingDto.getItem())
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", itemDto.getName());
        assertThat(bookingDto.getBooker())
                .hasFieldOrPropertyWithValue("id", bookerId)
                .hasFieldOrPropertyWithValue("name", booker.getName());
    }

    @Test
    @Transactional
    public void updateBooking_succeed() {
        assertThat(bookingDto = bookingService.createBooking(bookingDto, bookerId))
                .hasFieldOrPropertyWithValue("id", bookingDto.getId())
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING)
                .hasFieldOrPropertyWithValue("start", bookingDto.getStart())
                .hasFieldOrPropertyWithValue("end", bookingDto.getEnd());

        assertThat(bookingService.updateBooking(bookingDto.getId(), true, ownerId))
                .hasFieldOrPropertyWithValue("id", bookingDto.getId())
                .hasFieldOrPropertyWithValue("status", BookingStatus.APPROVED)
                .hasFieldOrPropertyWithValue("start", bookingDto.getStart())
                .hasFieldOrPropertyWithValue("end", bookingDto.getEnd());
    }

    @Test
    @Transactional
    public void findBookingById_succeed() {
        bookingDto = bookingService.createBooking(bookingDto, bookerId);

        assertThat(bookingService.findBookingById(bookingDto.getId(), ownerId)).isEqualTo(bookingDto);
    }

    @Test
    @Transactional
    public void findAllBookingsByOwnerIdAndStateCurrent_succeed() {
        bookingDto1 = bookingService.createBooking(bookingDto1, bookerId);
        bookingDto2 = bookingService.createBooking(bookingDto2, bookerId);
        assertThat(bookingService.findAllBookingsByOwnerIdAndState(bookerId, "CURRENT", 0, 20))
                .hasSize(2)
                .element(0)
                .isEqualTo(bookingDto2);
        assertThat(bookingService.findAllBookingsByOwnerIdAndState(bookerId, "CURRENT", 0, 20))
                .element(1)
                .isEqualTo(bookingDto1);
    }

    @Test
    @Transactional
    public void findAllBookingsByOwnerIdAndStatePast_succeed() {
        bookingDto1.setStart(LocalDateTime.now().minusSeconds(20));
        bookingDto1.setEnd(LocalDateTime.now().minusSeconds(15));
        bookingDto2.setStart(LocalDateTime.now().minusSeconds(10));
        bookingDto2.setEnd(LocalDateTime.now().minusSeconds(5));
        bookingDto1 = bookingService.createBooking(bookingDto1, bookerId);
        bookingDto2 = bookingService.createBooking(bookingDto2, bookerId);
        assertThat(bookingService.findAllBookingsByOwnerIdAndState(bookerId, "PAST", 0, 20))
                .hasSize(2)
                .element(0)
                .isEqualTo(bookingDto2);
        assertThat(bookingService.findAllBookingsByOwnerIdAndState(bookerId, "PAST", 0, 20))
                .element(1)
                .isEqualTo(bookingDto1);

    }

    @Test
    public void findAllBookingsByOwnerIdAndStateFuture_succeed() {
        bookingDto1.setStart(LocalDateTime.now().plusSeconds(5));
        bookingDto1.setEnd(LocalDateTime.now().plusSeconds(10));
        bookingDto2.setStart(LocalDateTime.now().plusSeconds(15));
        bookingDto2.setEnd(LocalDateTime.now().plusSeconds(20));

        bookingDto1 = bookingService.createBooking(bookingDto1, bookerId);
        bookingDto2 = bookingService.createBooking(bookingDto2, bookerId);

        assertThat(bookingService.findAllBookingsByOwnerIdAndState(bookerId, "FUTURE", 0, 20))
                .hasSize(2)
                .element(0)
                .isEqualTo(bookingDto2);
        assertThat(bookingService.findAllBookingsByOwnerIdAndState(bookerId, "FUTURE", 0, 20))
                .element(1)
                .isEqualTo(bookingDto1);
    }

    @Test
    @Transactional
    public void findAllBookingsByOwnerIdAndStateWaitingAndRejected_succeed() {
        bookingDto1 = bookingService.createBooking(bookingDto1, bookerId);
        bookingDto2 = bookingService.createBooking(bookingDto2, bookerId);

        bookingDto1 = bookingService.updateBooking(bookingDto1.getId(), false, ownerId);

        assertThat(bookingService.findAllBookingsByOwnerIdAndState(bookerId, "WAITING", 0, 20))
                .hasSize(1)
                .element(0)
                .isEqualTo(bookingDto2);
        assertThat(bookingService.findAllBookingsByOwnerIdAndState(bookerId, "REJECTED", 0, 20))
                .hasSize(1)
                .element(0)
                .isEqualTo(bookingDto1);
    }

    @Test
    @Transactional
    public void findAllBookingsForOwnerItemsWithStateCurrent_succeed() {
        bookingDto1 = bookingService.createBooking(bookingDto1, bookerId);
        bookingDto2 = bookingService.createBooking(bookingDto2, bookerId);
        assertThat(bookingService.findAllBookingsForOwnerItemsWithState(ownerId, "CURRENT", 0, 20))
                .hasSize(2)
                .element(0)
                .isEqualTo(bookingDto2);
        assertThat(bookingService.findAllBookingsForOwnerItemsWithState(ownerId, "CURRENT", 0, 20))
                .element(1)
                .isEqualTo(bookingDto1);
    }

    @Test
    @Transactional
    public void findAllBookingsForOwnerItemsWithStatePast_succeed() {
        bookingDto1.setStart(LocalDateTime.now().minusSeconds(20));
        bookingDto1.setEnd(LocalDateTime.now().minusSeconds(15));
        bookingDto2.setStart(LocalDateTime.now().minusSeconds(10));
        bookingDto2.setEnd(LocalDateTime.now().minusSeconds(5));
        bookingDto1 = bookingService.createBooking(bookingDto1, bookerId);
        bookingDto2 = bookingService.createBooking(bookingDto2, bookerId);

        assertThat(bookingService.findAllBookingsForOwnerItemsWithState(ownerId, "PAST", 0, 20))
                .hasSize(2)
                .element(0)
                .isEqualTo(bookingDto2);

        assertThat(bookingService.findAllBookingsForOwnerItemsWithState(ownerId, "PAST", 0, 20))
                .element(1)
                .isEqualTo(bookingDto1);
    }

    @Test
    @Transactional
    public void findAllBookingsForOwnerItemsWithStateFuture_succeed() {
        bookingDto1.setStart(LocalDateTime.now().plusSeconds(5));
        bookingDto1.setEnd(LocalDateTime.now().plusSeconds(10));
        bookingDto2.setStart(LocalDateTime.now().plusSeconds(15));
        bookingDto2.setEnd(LocalDateTime.now().plusSeconds(20));

        bookingDto1 = bookingService.createBooking(bookingDto1, bookerId);
        bookingDto2 = bookingService.createBooking(bookingDto2, bookerId);

        assertThat(bookingService.findAllBookingsForOwnerItemsWithState(ownerId, "FUTURE", 0, 20))
                .hasSize(2)
                .element(0)
                .isEqualTo(bookingDto2);
        assertThat(bookingService.findAllBookingsForOwnerItemsWithState(ownerId, "FUTURE", 0, 20))
                .element(1)
                .isEqualTo(bookingDto1);
    }

    @Test
    @Transactional
    public void findAllBookingsForOwnerItemsWithStateWaitingAndRejected_succeed() {
        bookingDto1 = bookingService.createBooking(bookingDto1, bookerId);
        bookingDto2 = bookingService.createBooking(bookingDto2, bookerId);

        bookingDto1 = bookingService.updateBooking(bookingDto1.getId(), false, ownerId);

        assertThat(bookingService.findAllBookingsForOwnerItemsWithState(ownerId, "WAITING", 0, 20))
                .hasSize(1)
                .element(0)
                .isEqualTo(bookingDto2);
        assertThat(bookingService.findAllBookingsForOwnerItemsWithState(ownerId, "REJECTED", 0, 20))
                .hasSize(1)
                .element(0)
                .isEqualTo(bookingDto1);
    }
}
