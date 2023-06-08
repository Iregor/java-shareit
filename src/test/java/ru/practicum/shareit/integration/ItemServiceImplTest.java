package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class ItemServiceImplTest {
    private final ItemServiceImpl itemService;
    private final UserServiceImpl userService;
    private final BookingServiceImpl bookingService;

    @Test
    public void createItem_succeed() {
        User user = new User(null, "user1", "email@email.ru");
        user = UserMapper.toUser(userService.createUser(user));
        ItemDto itemDto = ItemDto.builder().name("item1_name").description("item1_desc").available(true).build();


        assertThat(itemDto = itemService.createItem(itemDto, user.getId()))
                .hasFieldOrPropertyWithValue("id", itemDto.getId())
                .hasFieldOrPropertyWithValue("name", "item1_name")
                .hasFieldOrPropertyWithValue("description", "item1_desc");
    }

    @Test
    public void createItem_UserNotFound_exceptionThrown() {
        ItemDto itemDto = ItemDto.builder().name("item1_name").description("item1_desc").available(true).build();

        assertThatThrownBy(() -> itemService.createItem(itemDto, 100L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void createItem_RequestNotFound_exceptionThrown() {
        User user = new User(null, "user1", "email@email.ru");
        long userId = UserMapper.toUser(userService.createUser(user)).getId();
        ItemDto itemDto = ItemDto.builder().name("item1_name").description("item1_desc").available(true).requestId(100L).build();


        assertThatThrownBy(() -> itemService.createItem(itemDto, userId)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void updateItem_succeed() throws IllegalAccessException {
        User user = new User(null, "user1", "email@email.ru");
        user = UserMapper.toUser(userService.createUser(user));
        ItemDto itemDto = ItemDto.builder().name("item1_name").description("item1_desc").available(true).build();
        assertThat(itemDto = itemService.createItem(itemDto, user.getId()))
                .hasFieldOrPropertyWithValue("name", "item1_name")
                .hasFieldOrPropertyWithValue("description", "item1_desc");

        long itemId = itemDto.getId();
        itemDto = ItemDto.builder().name("updated_name").description("updated_description").available(false).build();
        assertThat(itemService.updateItem(itemDto, user.getId(), itemId))
                .hasFieldOrPropertyWithValue("name", "updated_name")
                .hasFieldOrPropertyWithValue("description", "updated_description")
                .hasFieldOrPropertyWithValue("available", false);
    }

    @Test
    public void findItemById_succeed() {
        User owner = new User(null, "owner", "owner@email.ru");
        long ownerId = userService.createUser(owner).getId();
        User booker = new User(null, "booker", "booker@email.ru");
        long bookerId = userService.createUser(booker).getId();

        ItemDto itemDto = ItemDto.builder().name("item1_name").description("item1_desc").available(true).build();
        long itemId = itemService.createItem(itemDto, ownerId).getId();

        BookingDto lastBookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusSeconds(20))
                .end(LocalDateTime.now().minusSeconds(10))
                .build();
        BookingDto nextBookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusSeconds(10))
                .end(LocalDateTime.now().plusSeconds(20))
                .build();

        long lastBookingId = bookingService.createBooking(lastBookingDto, bookerId).getId();
        long nextBookingId = bookingService.createBooking(nextBookingDto, bookerId).getId();
        bookingService.updateBooking(lastBookingId, true, ownerId);
        bookingService.updateBooking(nextBookingId, true, ownerId);

        ItemWithBookingsDto resultDto;
        assertThat(resultDto = (ItemWithBookingsDto) itemService.findItemById(itemId, ownerId))
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", itemDto.getName())
                .hasFieldOrPropertyWithValue("description", itemDto.getDescription());
        assertThat(resultDto.getLastBooking())
                .hasFieldOrPropertyWithValue("id", lastBookingId)
                .hasFieldOrPropertyWithValue("bookerId", bookerId);
        assertThat(resultDto.getNextBooking())
                .hasFieldOrPropertyWithValue("id", nextBookingId)
                .hasFieldOrPropertyWithValue("bookerId", bookerId);
    }

    @Test
    public void findOwnerItems_succeed() {
        User owner = new User(null, "owner", "owner@email.ru");
        long ownerId = userService.createUser(owner).getId();
        User other = new User(null, "other", "other@email.ru");
        long otherId = userService.createUser(other).getId();

        ItemDto item1Dto = ItemDto.builder().name("item1_name").description("item1_desc").available(true).build();
        long item1Id = itemService.createItem(item1Dto, ownerId).getId();
        ItemDto item2Dto = ItemDto.builder().name("item2_name").description("item2_desc").available(true).build();
        long item2Id = itemService.createItem(item2Dto, ownerId).getId();
        ItemDto item3Dto = ItemDto.builder().name("item3_name").description("item3_desc").available(true).build();
        long item3Id = itemService.createItem(item3Dto, otherId).getId();

        assertThat(itemService.findOwnerItems(ownerId))
                .hasSize(2)
                .element(0)
                .hasFieldOrPropertyWithValue("id", item1Id);
        assertThat(itemService.findOwnerItems(ownerId))
                .element(1)
                .hasFieldOrPropertyWithValue("id", item2Id);
    }

    @Test
    public void searchAvailableItems_succeed() throws IllegalAccessException {
        User owner = new User(null, "owner", "owner@email.ru");
        long ownerId = userService.createUser(owner).getId();
        ItemDto item1Dto = ItemDto.builder().name("item1_name").description("item1_desc").available(true).build();
        long item1Id = itemService.createItem(item1Dto, ownerId).getId();
        ItemDto item2Dto = ItemDto.builder().name("item2_name").description("item2_desc").available(true).build();
        long item2Id = itemService.createItem(item2Dto, ownerId).getId();
        ItemDto item3Dto = ItemDto.builder().name("name").description("desc").available(true).build();
        long item3Id = itemService.createItem(item3Dto, ownerId).getId();


        assertThat(itemService.searchAvailableItems("item")).hasSize(2);

        item2Dto = ItemDto.builder().name("item2_name").description("item2_desc").available(false).build();
        itemService.updateItem(item2Dto, ownerId, item2Id);
        assertThat(itemService.searchAvailableItems("item"))
                .hasSize(1)
                .element(0)
                .hasFieldOrPropertyWithValue("id", item1Id)
                .hasFieldOrPropertyWithValue("name", item1Dto.getName())
                .hasFieldOrPropertyWithValue("description", item1Dto.getDescription());
    }

    @Test
    public void createComment_succeed() {
        User owner = new User(null, "owner", "owner@email.ru");
        long ownerId = userService.createUser(owner).getId();
        User booker = new User(null, "booker", "booker@email.ru");
        long bookerId = userService.createUser(booker).getId();

        ItemDto itemDto = ItemDto.builder().name("item1_name").description("item1_desc").available(true).build();
        long itemId = itemService.createItem(itemDto, ownerId).getId();

        BookingDto lastBookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusSeconds(20))
                .end(LocalDateTime.now().minusSeconds(10))
                .build();

        long lastBookingId = bookingService.createBooking(lastBookingDto, bookerId).getId();
        bookingService.updateBooking(lastBookingId, true, ownerId);

        CommentDto commentDto = CommentDto.builder().text("comment_text").build();
        assertThat(commentDto = itemService.createComment(commentDto, itemId, bookerId))
                .hasFieldOrPropertyWithValue("id", commentDto.getId())
                .hasFieldOrPropertyWithValue("authorName", booker.getName());
    }
}