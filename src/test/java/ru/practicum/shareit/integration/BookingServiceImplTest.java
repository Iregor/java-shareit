package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
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

    @Test
    @Transactional
    public void createBooking_succeed() {
        User owner = new User(null, "owner", "owner@email.ru");
        long ownerId = userService.createUser(owner).getId();
        User booker = new User(null, "booker", "booker@email.ru");
        long bookerId = userService.createUser(booker).getId();

        ItemDto itemDto = ItemDto.builder().name("item1_name").description("item1_desc").available(true).build();
        long itemId = itemService.createItem(itemDto, ownerId).getId();

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusSeconds(10))
                .end(LocalDateTime.now().plusSeconds(20))
                .build();

        assertThat(bookingDto = bookingService.createBooking(bookingDto, bookerId))
                .hasFieldOrPropertyWithValue("id", bookingDto.getId())
                .extracting(BookingDto::getBooker)
                .hasFieldOrPropertyWithValue("id", bookerId);
        assertThat(bookingDto).extracting(BookingDto::getItem).hasFieldOrPropertyWithValue("id", itemId);
    }

    @Test
    @Transactional
    public void updateBooking_succeed() {
        User owner = new User(null, "owner", "owner@email.ru");
        long ownerId = userService.createUser(owner).getId();
        User booker = new User(null, "booker", "booker@email.ru");
        long bookerId = userService.createUser(booker).getId();

        ItemDto itemDto = ItemDto.builder().name("item1_name").description("item1_desc").available(true).build();
        long itemId = itemService.createItem(itemDto, ownerId).getId();

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusSeconds(10))
                .end(LocalDateTime.now().plusSeconds(20))
                .build();

        assertThat(bookingDto = bookingService.createBooking(bookingDto, bookerId))
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING);
        assertThat(bookingDto = bookingService.updateBooking(bookingDto.getId(), true, ownerId))
                .hasFieldOrPropertyWithValue("status", BookingStatus.APPROVED);
    }

    @Test
    @Transactional
    public void findBookingById_succeed() {
        User owner = new User(null, "owner", "owner@email.ru");
        long ownerId = userService.createUser(owner).getId();
        User booker = new User(null, "booker", "booker@email.ru");
        long bookerId = userService.createUser(booker).getId();

        ItemDto itemDto = ItemDto.builder().name("item1_name").description("item1_desc").available(true).build();
        long itemId = itemService.createItem(itemDto, ownerId).getId();

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusSeconds(10))
                .end(LocalDateTime.now().plusSeconds(20))
                .build();

        bookingDto = bookingService.createBooking(bookingDto, bookerId);

        assertThat(bookingService.findBookingById(bookingDto.getId(), ownerId)).isEqualTo(bookingDto);
    }

    @Test
    @Transactional
    public void findAllBookingsByOwnerIdAndState_succeed() {
        User owner = new User(null, "owner", "owner@email.ru");
        long ownerId = userService.createUser(owner).getId();
        User booker = new User(null, "booker", "booker@email.ru");
        long bookerId = userService.createUser(booker).getId();

        ItemDto itemDto = ItemDto.builder().name("item1_name").description("item1_desc").available(true).build();
        long itemId = itemService.createItem(itemDto, ownerId).getId();

        BookingDto bookingDto1 = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusSeconds(10))
                .end(LocalDateTime.now().plusSeconds(10))
                .build();
        BookingDto bookingDto2 = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusSeconds(5))
                .end(LocalDateTime.now().plusSeconds(5))
                .build();
        bookingDto1 = bookingService.createBooking(bookingDto1, bookerId);
        bookingDto2 = bookingService.createBooking(bookingDto2, bookerId);
        assertThat(bookingService.findAllBookingsByOwnerIdAndState(bookerId, "CURRENT", 0, 20))
                .hasSize(2)
                .element(0)
                .hasFieldOrPropertyWithValue("id", bookingDto2.getId());

        bookingDto1 = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusSeconds(10))
                .end(LocalDateTime.now().minusSeconds(5))
                .build();
        bookingDto2 = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusSeconds(5))
                .end(LocalDateTime.now().minusSeconds(2))
                .build();
        bookingDto1 = bookingService.createBooking(bookingDto1, bookerId);
        bookingDto2 = bookingService.createBooking(bookingDto2, bookerId);
        assertThat(bookingService.findAllBookingsByOwnerIdAndState(bookerId, "PAST", 0, 20))
                .hasSize(2)
                .element(0)
                .hasFieldOrPropertyWithValue("id", bookingDto2.getId());

        bookingDto1 = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusSeconds(10))
                .end(LocalDateTime.now().plusSeconds(20))
                .build();
        bookingDto2 = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusSeconds(30))
                .end(LocalDateTime.now().plusSeconds(40))
                .build();
        bookingDto1 = bookingService.createBooking(bookingDto1, bookerId);
        bookingDto2 = bookingService.createBooking(bookingDto2, bookerId);
        assertThat(bookingService.findAllBookingsByOwnerIdAndState(bookerId, "FUTURE", 0, 20))
                .hasSize(2)
                .element(0)
                .hasFieldOrPropertyWithValue("id", bookingDto2.getId());

        bookingDto1 = bookingService.updateBooking(bookingDto1.getId(), false, ownerId);

        assertThat(bookingService.findAllBookingsByOwnerIdAndState(bookerId, "WAITING", 0, 20))
                .element(0)
                .hasFieldOrPropertyWithValue("id", bookingDto2.getId());
        assertThat(bookingService.findAllBookingsByOwnerIdAndState(bookerId, "REJECTED", 0, 20))
                .element(0)
                .hasFieldOrPropertyWithValue("id", bookingDto1.getId());
    }

    @Test
    @Transactional
    public void findAllBookingsForOwnerItemsWithState_succeed() {
        User owner = new User(null, "owner", "owner@email.ru");
        long ownerId = userService.createUser(owner).getId();
        User booker = new User(null, "booker", "booker@email.ru");
        long bookerId = userService.createUser(booker).getId();

        ItemDto itemDto = ItemDto.builder().name("item1_name").description("item1_desc").available(true).build();
        long itemId = itemService.createItem(itemDto, ownerId).getId();

        BookingDto bookingDto1 = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusSeconds(10))
                .end(LocalDateTime.now().plusSeconds(10))
                .build();
        BookingDto bookingDto2 = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusSeconds(5))
                .end(LocalDateTime.now().plusSeconds(5))
                .build();
        bookingDto1 = bookingService.createBooking(bookingDto1, bookerId);
        bookingDto2 = bookingService.createBooking(bookingDto2, bookerId);
        assertThat(bookingService.findAllBookingsForOwnerItemsWithState(ownerId, "CURRENT", 0, 20))
                .hasSize(2)
                .element(0)
                .hasFieldOrPropertyWithValue("id", bookingDto2.getId());

        bookingDto1 = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusSeconds(10))
                .end(LocalDateTime.now().minusSeconds(5))
                .build();
        bookingDto2 = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusSeconds(5))
                .end(LocalDateTime.now().minusSeconds(2))
                .build();
        bookingDto1 = bookingService.createBooking(bookingDto1, bookerId);
        bookingDto2 = bookingService.createBooking(bookingDto2, bookerId);
        assertThat(bookingService.findAllBookingsForOwnerItemsWithState(ownerId, "PAST", 0, 20))
                .hasSize(2)
                .element(0)
                .hasFieldOrPropertyWithValue("id", bookingDto2.getId());

        bookingDto1 = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusSeconds(10))
                .end(LocalDateTime.now().plusSeconds(20))
                .build();
        bookingDto2 = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusSeconds(30))
                .end(LocalDateTime.now().plusSeconds(40))
                .build();
        bookingDto1 = bookingService.createBooking(bookingDto1, bookerId);
        bookingDto2 = bookingService.createBooking(bookingDto2, bookerId);
        assertThat(bookingService.findAllBookingsForOwnerItemsWithState(ownerId, "FUTURE", 0, 20))
                .hasSize(2)
                .element(0)
                .hasFieldOrPropertyWithValue("id", bookingDto2.getId());

        bookingDto1 = bookingService.updateBooking(bookingDto1.getId(), false, ownerId);

        assertThat(bookingService.findAllBookingsForOwnerItemsWithState(ownerId, "WAITING", 0, 20))
                .element(0)
                .hasFieldOrPropertyWithValue("id", bookingDto2.getId());
        assertThat(bookingService.findAllBookingsForOwnerItemsWithState(ownerId, "REJECTED", 0, 20))
                .element(0)
                .hasFieldOrPropertyWithValue("id", bookingDto1.getId());
    }
}
