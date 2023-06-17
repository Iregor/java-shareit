package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.RequestDesc;
import ru.practicum.shareit.request.dto.RequestWithItems;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.request.service.RequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {
    @InjectMocks
    RequestServiceImpl requestService;

    @Mock
    RequestRepository requestRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;

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
        MockBehaviourManager.setRequestRepositoryBehaviour(requestRepository, requests);
    }

    /*RequestWithItems createRequest(RequestDesc requestDesc, long userId) - создание запроса пользователем.
     * requestDesci содержит поле description с описанием запрашиваемой вещи.
     * userId - id пользователя, создающего запрос. */
    @Test
    public void createRequestTest_validRequestValidUser_succeed() {
        RequestDesc dto = new RequestDesc("sth like item1");
        assertThat(requestService.createRequest(dto, user1.getId()))
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("description", "sth like item1");
    }

    @Test
    public void createRequestTest_notExistingUser_exceptionThrown() {
        RequestDesc dto = new RequestDesc("sth like item1");
        assertThatThrownBy(() -> requestService.createRequest(dto, 100L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    /*List<RequestWithItems> findAllOwnersRequests(long userId) - найти все запросы пользователя. */
    @Test
    public void findAllOwnersRequestsTest_forValidUserWithRequests_succeed() {
        request1.setRequestor(user1);
        request2.setRequestor(user1);

        assertThat(requestService.findAllOwnersRequests(user1.getId()))
                .hasSize(2)
                .element(0).hasFieldOrPropertyWithValue("id", request1.getId());

        assertThat(requestService.findAllOwnersRequests(user1.getId()))
                .element(1).hasFieldOrPropertyWithValue("id", request2.getId());
    }

    @Test
    public void findAllOwnersRequestsTest_forNotExistingUser_exceptionThrown() {
        assertThatThrownBy(() -> requestService.findAllOwnersRequests(100L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void findAllOwnersRequestsTest_userWithoutRequests_succeed() {
        assertThat(requestService.findAllOwnersRequests(user1.getId())).hasSize(0);
    }

    /*List<RequestWithItems> findAllRequests(long userId, int from, int size) - поиск всех запросов.*/
    @Test
    public void findAllRequestsTest_validIncome_succeed() {
        request1.setRequestor(user1);
        request2.setRequestor(user2);
        request3.setRequestor(user1);
        assertThat(requestService.findAllRequests(user2.getId(), 0, 20))
                .hasSize(2)
                .element(0)
                .hasFieldOrPropertyWithValue("id", request1.getId());
        assertThat(requestService.findAllRequests(user2.getId(), 0, 20))
                .element(1).hasFieldOrPropertyWithValue("id", request3.getId());
    }

    @Test
    public void findAllRequestsTest_notExistingUser_exceptionThrown() {
        assertThatThrownBy(() -> requestService.findAllRequests(100L, 0, 20))
                .isInstanceOf(EntityNotFoundException.class);
    }

    /*RequestWithItems findRequestById(long userId, long requestId) - поиск запроса по id*/
    @Test
    public void findRequestByIdTest_validIncome_succeed() {
        request1.setRequestor(user1);
        item1.setOwner(user2);
        item1.setItemRequest(request1);

        RequestWithItems dtoExpected = RequestMapper.toRequestWithItems(request1, List.of(item1));

        assertThat(requestService.findRequestById(user1.getId(), request1.getId())).isEqualTo(dtoExpected);
    }

    @Test
    public void findRequestByIdTest_notExistingUser_exceptionThrown() {
        assertThatThrownBy(() -> requestService.findRequestById(100L, request1.getId())).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void findRequestByIdTest_notExistingRequest_exceptionThrown() {
        assertThatThrownBy(() -> requestService.findRequestById(user1.getId(), 100L)).isInstanceOf(EntityNotFoundException.class);
    }
}