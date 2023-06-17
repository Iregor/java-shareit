package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exception.exceptions.IllegalAccessToEntityException;
import ru.practicum.shareit.exception.exceptions.ItemIdNotConsistentException;
import ru.practicum.shareit.exception.exceptions.NoResolvedBookingException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @InjectMocks
    ItemServiceImpl itemService;
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    RequestRepository requestRepository;

    User user1, user2, user3;
    Item item1, item2, item3;
    Request request1, request2, request3;
    Booking booking1, booking2, booking3;
    HashMap<Long, User> users;
    HashMap<Long, Item> items;
    HashMap<Long, Booking> bookings;
    HashMap<Long, Request> requests;
    HashMap<Long, Comment> comments;

    @BeforeEach
    public void prepareTestEnvironment() {
        createEntities();
        setMocksBehaviour();
    }

    public void createEntities() {
        bookings = new HashMap<>();
        booking1 = new Booking(1L, null, null, LocalDateTime.now(), LocalDateTime.now().plusDays(1), BookingStatus.WAITING);
        booking2 = new Booking(2L, null, null, LocalDateTime.now(), LocalDateTime.now().plusDays(1), BookingStatus.WAITING);
        booking3 = new Booking(3L, null, null, LocalDateTime.now(), LocalDateTime.now().plusDays(1), BookingStatus.WAITING);
        bookings.put(booking1.getId(), booking1);
        bookings.put(booking2.getId(), booking2);
        bookings.put(booking3.getId(), booking3);

        users = new HashMap<>();
        user1 = new User(1L, "user1", "email1@yandex.ru");
        user2 = new User(2L, "user2", "email2@yandex.ru");
        user3 = new User(3L, "user3", "email3@yandex.ru");
        users.put(user1.getId(), user1);
        users.put(user2.getId(), user2);
        users.put(user3.getId(), user3);

        items = new HashMap<>();
        item1 = new Item(1L, "item1_name", "item1_desc", true, null, null);
        item2 = new Item(2L, "item2_name", "item2_desc", true, null, null);
        item3 = new Item(3L, "item3_name", "item3_desc", true, null, null);
        items.put(item1.getId(), item1);
        items.put(item2.getId(), item2);
        items.put(item3.getId(), item3);

        requests = new HashMap<>();
        request1 = new Request(1L, "request1_desc", null, LocalDateTime.now());
        request2 = new Request(2L, "request2_desc", null, LocalDateTime.now());
        request3 = new Request(3L, "request3_desc", null, LocalDateTime.now());
        requests.put(request1.getId(), request1);
        requests.put(request2.getId(), request2);
        requests.put(request3.getId(), request3);

        comments = new HashMap<>();
    }

    public void setMocksBehaviour() {
        MockBehaviourManager.setUserRepositoryBehaviour(userRepository, users);
        MockBehaviourManager.setItemRepositoryBehaviour(itemRepository, items);
        MockBehaviourManager.setBookingRepositoryBehaviour(bookingRepository, bookings);
        MockBehaviourManager.setRequestRepositoryBehaviour(requestRepository, requests);
        MockBehaviourManager.setCommentRepositoryBehaviour(commentRepository, comments);
    }

    /*ItemDto createItem(ItemDto itemDto, Long userId) - создание вещи владельцем
    Может быть создана с ответом на существующий Request.*/
    @Test
    public void createItemTest_validItemNoRequest_succeed() {
        item1.setOwner(user1);
        ItemDto dto = ItemDto.builder().name(item1.getName()).description(item1.getDescription()).available(item1.getAvailable()).build();

        assertThat(itemService.createItem(dto, user1.getId())).isEqualTo(ItemMapper.toItemDto(item1));
    }

    @Test
    public void createItemTest_validItemWithRequest_succeed() {
        item1.setOwner(user1);
        item1.setItemRequest(request1);
        request1.setRequestor(user2);

        ItemDto dto = ItemDto.builder().name(item1.getName()).description(item1.getDescription()).available(item1.getAvailable()).requestId(request1.getId()).build();

        assertThat(itemService.createItem(dto, user1.getId())).isEqualTo(ItemMapper.toItemDto(item1));
    }

    @Test
    public void createItemTest_failUser_exceptionThrown() {
        item1.setOwner(user1);
        item1.setItemRequest(request1);
        request1.setRequestor(user2);

        ItemDto dto = ItemDto.builder().name(item1.getName()).description(item1.getDescription()).available(item1.getAvailable()).requestId(request1.getId()).build();

        assertThatThrownBy(() -> itemService.createItem(dto, 100L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void createItemTest_failRequest_exceptionThrown() {
        item1.setOwner(user1);
        item1.setItemRequest(request1);
        request1.setRequestor(user2);

        ItemDto dto = ItemDto.builder().name(item1.getName()).description(item1.getDescription()).available(item1.getAvailable()).requestId(100L).build();

        assertThatThrownBy(() -> itemService.createItem(dto, user1.getId())).isInstanceOf(EntityNotFoundException.class);
    }

    /*ItemDto updateItem(ItemDto itemDto, Long userId, Long itemId) - обновление вещи.
     * Редактировать может только владелец.*/

    @Test
    public void updateItem_validIncome_succeed() throws IllegalAccessException {
        item1.setOwner(user1);
        item1.setItemRequest(request1);
        request1.setRequestor(user2);
        item1.setName("updated_name");

        ItemDto dto = ItemDto.builder().id(item1.getId()).name("updated_name").build();

        assertThat(itemService.updateItem(dto, user1.getId(), item1.getId())).isEqualTo(ItemMapper.toItemDto(item1));
    }

    @Test
    public void updateItem_userNotOwner_exceptionThrown() {
        item1.setOwner(user2);
        item1.setItemRequest(request1);
        request1.setRequestor(user2);
        item1.setName("updated_name");

        ItemDto dto = ItemDto.builder().id(item1.getId()).name("updated_name").build();

        assertThatThrownBy(() -> itemService.updateItem(dto, user1.getId(), item1.getId())).isInstanceOf(IllegalAccessToEntityException.class);
    }

    @Test
    public void updateItem_itemNotConsistency_exceptionThrown() {
        item1.setOwner(user1);
        item1.setItemRequest(request1);
        request1.setRequestor(user2);
        item1.setName("updated_name");

        ItemDto dto = ItemDto.builder().id(item2.getId()).name("updated_name").build();

        assertThatThrownBy(() -> itemService.updateItem(dto, user1.getId(), item1.getId())).isInstanceOf(ItemIdNotConsistentException.class);
    }

    /*ItemDto findItemById(Long itemId, Long userId) - просмотр информации о вещи
     * Может просматривать любой пользователь.
     * Бронирования видны только при запросе владельцем. */
    @Test
    public void findItemByIdTest_byOwnerWithBookingsNoComments_succeed() {
        item1.setOwner(user1);
        booking1.setItem(item1);
        booking1.setBooker(user2);
        booking1.setStart(LocalDateTime.now().minusSeconds(10));
        booking2.setItem(item1);
        booking2.setBooker(user2);
        booking2.setStart(LocalDateTime.now().plusSeconds(10));

        ItemWithBookingsDto dto = ItemMapper.toItemWithBookingsDto(item1);
        dto.setLastBooking(BookingMapper.toBookingBookerIdDto(booking1));
        dto.setNextBooking(BookingMapper.toBookingBookerIdDto(booking2));
        dto.setComments(new ArrayList<>());

        Mockito.when(bookingRepository.findFirstByItemIdAndStartBeforeAndStatusOrderByEndDesc(Mockito.anyLong(), Mockito.any(), Mockito.any()))
                .thenReturn(Optional.of(booking1));
        Mockito.when(bookingRepository.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(Mockito.anyLong(), Mockito.any(), Mockito.any()))
                .thenReturn(Optional.of(booking2));

        assertThat(itemService.findItemById(item1.getId(), user1.getId())).isEqualTo(dto);
    }

    @Test
    public void findItemByIdTest_byOtherUserWithBookingsNoComments_succeed() {
        item1.setOwner(user1);
        booking1.setItem(item1);
        booking1.setBooker(user2);
        booking1.setStart(LocalDateTime.now().minusSeconds(10));
        booking2.setItem(item1);
        booking2.setBooker(user2);
        booking2.setStart(LocalDateTime.now().plusSeconds(10));

        assertThat(itemService.findItemById(item1.getId(), user3.getId())).extracting("lastBooking").isEqualTo(null);
        assertThat(itemService.findItemById(item1.getId(), user3.getId())).extracting("nextBooking").isEqualTo(null);
    }

    @Test
    public void findItemByIdTest_notExistingItem_thrownException() {
        assertThatThrownBy(() -> itemService.findItemById(100L, user1.getId())).isInstanceOf(EntityNotFoundException.class);
    }

    /*List<ItemDto> findOwnerItems(Long userId) - просмотр вещей пользователя.
     * Может быть выполнено только пользователем.*/

    @Test
    public void findOwnerItemsTest_validIncome_succeed() {
        item1.setOwner(user1);
        booking1.setItem(item1);
        booking1.setBooker(user2);
        booking1.setStart(LocalDateTime.now().minusSeconds(10));
        booking2.setItem(item1);
        booking2.setBooker(user2);
        booking2.setStart(LocalDateTime.now().plusSeconds(10));

        ItemWithBookingsDto dto = ItemMapper.toItemWithBookingsDto(item1);
        dto.setLastBooking(BookingMapper.toBookingBookerIdDto(booking1));
        dto.setNextBooking(BookingMapper.toBookingBookerIdDto(booking2));
        dto.setComments(new ArrayList<>());

        Mockito.when(bookingRepository.findFirstByItemIdAndStartBeforeAndStatusOrderByEndDesc(Mockito.anyLong(), Mockito.any(), Mockito.any()))
                .thenReturn(Optional.of(booking1));
        Mockito.when(bookingRepository.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(Mockito.anyLong(), Mockito.any(), Mockito.any()))
                .thenReturn(Optional.of(booking2));
        Mockito.when(itemRepository.findAllByOwnerIdOrderByIdAsc(Mockito.anyLong()))
                .thenReturn(List.of(item1));

        assertThat(itemService.findOwnerItems(user1.getId())).hasSize(1).element(0).isEqualTo(dto);
    }

    @Test
    public void findOwnerItemsTest_notExistingUser_exceptionThrown() {
        assertThatThrownBy(() -> itemService.findOwnerItems(100L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void searchAvailableItemsTest_existedText_succeed() {
        item1.setOwner(user1);
        item2.setOwner(user1);
        item3.setOwner(user1);
        assertThat(itemService.searchAvailableItems("desc")).hasSize(3);
    }

    @Test
    public void searchAvailableItemsTest_notExistingText_emptyList() {
        item1.setOwner(user1);
        item2.setOwner(user1);
        item3.setOwner(user1);
        assertThat(itemService.searchAvailableItems("no such text")).hasSize(0);
    }

    @Test
    public void searchAvailableItemsTest_blankText_emptyList() {
        item1.setOwner(user1);
        item2.setOwner(user1);
        item3.setOwner(user1);
        assertThat(itemService.searchAvailableItems("   ")).hasSize(0);
    }

    @Test
    public void createCommentTest_forResolvedBooking_succeed() {
        booking1.setStatus(BookingStatus.APPROVED);
        booking1.setStart(LocalDateTime.now().minusSeconds(20));
        booking1.setEnd(LocalDateTime.now().minusSeconds(5));
        booking1.setBooker(user1);
        booking1.setItem(item1);
        item1.setOwner(user2);
        CommentDto dto = CommentDto.builder().text("comment1").build();
        assertThat(itemService.createComment(dto, item1.getId(), user1.getId()))
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("authorName", user1.getName())
                .hasFieldOrPropertyWithValue("text", dto.getText());
    }

    @Test
    public void createCommentTest_forNotApprovedBooking_exceptionThrown() {
        booking1.setStatus(BookingStatus.WAITING);
        booking1.setStart(LocalDateTime.now().minusSeconds(20));
        booking1.setEnd(LocalDateTime.now().minusSeconds(5));
        booking1.setBooker(user1);
        booking1.setItem(item1);
        item1.setOwner(user2);
        CommentDto dto = CommentDto.builder().text("comment1").build();
        assertThatThrownBy(() -> itemService.createComment(dto, item1.getId(), user1.getId()))
                .isInstanceOf(NoResolvedBookingException.class);
    }

    @Test
    public void createCommentTest_forFutureBooking_exceptionThrown() {
        booking1.setStatus(BookingStatus.APPROVED);
        booking1.setStart(LocalDateTime.now().plusSeconds(20));
        booking1.setEnd(LocalDateTime.now().plusSeconds(30));
        booking1.setBooker(user1);
        booking1.setItem(item1);
        item1.setOwner(user2);
        CommentDto dto = CommentDto.builder().text("comment1").build();
        assertThatThrownBy(() -> itemService.createComment(dto, item1.getId(), user1.getId()))
                .isInstanceOf(NoResolvedBookingException.class);
    }
}