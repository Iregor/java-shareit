package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
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
    User user, owner, booker;
    ItemDto itemDto, item1Dto, item2Dto, item3Dto;
    BookingDto lastBookingDto, nextBookingDto;
    long userId, bookerId, ownerId;

    @BeforeEach
    public void createEntities() {
        user = new User(null, "user1", "email@email.ru");
        owner = new User(null, "owner", "owner@email.ru");
        booker = new User(null, "booker", "booker@email.ru");
        user = UserMapper.toUser(userService.createUser(user));
        owner = UserMapper.toUser(userService.createUser(owner));
        booker = UserMapper.toUser(userService.createUser(booker));
        userId = user.getId();
        ownerId = owner.getId();
        bookerId = booker.getId();

        lastBookingDto = BookingDto.builder()
                .itemId(null)
                .start(LocalDateTime.now().minusSeconds(20))
                .end(LocalDateTime.now().minusSeconds(10))
                .build();
        nextBookingDto = BookingDto.builder()
                .itemId(null)
                .start(LocalDateTime.now().plusSeconds(10))
                .end(LocalDateTime.now().plusSeconds(20))
                .build();

        itemDto = ItemDto.builder().name("item1_name").description("item1_desc").available(true).build();
        item1Dto = ItemDto.builder().name("item1_name").description("item1_desc").available(true).build();
        item2Dto = ItemDto.builder().name("item2_name").description("item2_desc").available(true).build();
        item3Dto = ItemDto.builder().name("item3_name").description("item3_desc").available(true).build();
    }

    @Test
    public void createItem_succeed() {
        assertThat(itemDto = itemService.createItem(itemDto, user.getId()))
                .hasFieldOrPropertyWithValue("id", itemDto.getId())
                .hasFieldOrPropertyWithValue("name", "item1_name")
                .hasFieldOrPropertyWithValue("description", "item1_desc");
    }

    @Test
    public void createItem_UserNotFound_exceptionThrown() {
        assertThatThrownBy(() -> itemService.createItem(itemDto, 100L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void createItem_RequestNotFound_exceptionThrown() {
        long notExistingRequestId = 100L;
        itemDto.setRequestId(notExistingRequestId);

        assertThatThrownBy(() -> itemService.createItem(itemDto, user.getId())).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void updateItem_succeed() throws IllegalAccessException {
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
        long itemId = itemService.createItem(itemDto, ownerId).getId();

        lastBookingDto.setItemId(itemId);
        nextBookingDto.setItemId(itemId);

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
        long item1Id = itemService.createItem(item1Dto, ownerId).getId();
        long item2Id = itemService.createItem(item2Dto, ownerId).getId();
        long item3Id = itemService.createItem(item3Dto, userId).getId();

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
        long ownerId = userService.createUser(owner).getId();
        long item1Id = itemService.createItem(item1Dto, ownerId).getId();
        long item2Id = itemService.createItem(item2Dto, ownerId).getId();
        item3Dto.setName("name");
        item3Dto.setDescription("desc");
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
        long itemId = itemService.createItem(itemDto, ownerId).getId();

        lastBookingDto.setItemId(itemId);

        long lastBookingId = bookingService.createBooking(lastBookingDto, bookerId).getId();
        bookingService.updateBooking(lastBookingId, true, ownerId);

        CommentDto commentDto = CommentDto.builder().text("comment_text").build();
        assertThat(commentDto = itemService.createComment(commentDto, itemId, bookerId))
                .hasFieldOrPropertyWithValue("id", commentDto.getId())
                .hasFieldOrPropertyWithValue("authorName", booker.getName());
    }
}