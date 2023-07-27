package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.exceptions.BookingStatusAlreadyApprovedException;
import ru.practicum.shareit.exception.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exception.exceptions.IllegalAccessToEntityException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @InjectMocks
    BookingServiceImpl bookingService;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;
    User user1, user2, user3;
    Item item1, item2, item3;
    Booking booking1, booking2, booking3;
    HashMap<Long, User> users;
    HashMap<Long, Item> items;
    HashMap<Long, Booking> bookings;

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
    }

    public void setMocksBehaviour() {
        MockBehaviourManager.setUserRepositoryBehaviour(userRepository, users);
        MockBehaviourManager.setItemRepositoryBehaviour(itemRepository, items);
        MockBehaviourManager.setBookingRepositoryBehaviour(bookingRepository, bookings);
    }

    @Test
    public void createBookingValidEntityTest() {
        BookingDto dto = BookingDto.builder()
                .itemId(item1.getId())
                .start(booking1.getStart())
                .end(booking1.getEnd())
                .build();

        booking1.setItem(item1);
        booking1.setBooker(user1);
        item1.setOwner(user2);

        assertThat((bookingService.createBooking(dto, user1.getId()))).isEqualTo(BookingMapper.toDto(booking1));
    }

    @Test
    public void createBookingByItemOwnerExceptionThrown() {
        BookingDto dto = BookingDto.builder()
                .itemId(item1.getId())
                .start(booking1.getStart())
                .end(booking1.getEnd())
                .build();

        booking1.setItem(item1);
        booking1.setBooker(user1);
        item1.setOwner(user1);

        assertThatThrownBy(() -> bookingService.createBooking(dto, user1.getId())).isInstanceOf(IllegalAccessToEntityException.class);
    }

    /*  BookingDto updateBooking(Long bookingId, boolean approved, Long ownerId) - обновление бронирования
    Может быть выполнено по существующему бронированию
    Может быть выполнено только владельцем бронируемой вещи
    Может быть выполнено по вещи без статуса APPROVED*/

    @Test
    public void updateBooking_correctIncomeAgree_succeed_Test() {
        booking1.setItem(item1);
        booking1.setBooker(user1);
        item1.setOwner(user1);
        Booking booking = copyOf(booking1);
        booking.setStatus(BookingStatus.APPROVED);

        assertThat(bookingService.updateBooking(booking1.getId(), true, user1.getId())).isEqualTo(BookingMapper.toDto(booking));
    }

    @Test
    public void updateBooking_correctIncomeDecline_succeed_Test() {
        booking1.setItem(item1);
        booking1.setBooker(user1);
        item1.setOwner(user1);
        Booking booking = copyOf(booking1);
        booking.setStatus(BookingStatus.REJECTED);

        assertThat(bookingService.updateBooking(booking1.getId(), false, user1.getId())).isEqualTo(BookingMapper.toDto(booking));
    }


    @Test
    public void updateBooking_notOwner_ExceptionThrown_Test() {
        booking1.setItem(item1);
        booking1.setBooker(user1);
        item1.setOwner(user2);
        Booking booking = copyOf(booking1);
        booking.setStatus(BookingStatus.APPROVED);

        assertThatThrownBy(() -> bookingService.updateBooking(booking1.getId(), true, user1.getId())).isInstanceOf(IllegalAccessToEntityException.class);
    }

    @Test
    public void updateBooking_notExistingBooking_ExceptionThrown_Test() {
        booking1.setItem(item1);
        booking1.setBooker(user1);
        item1.setOwner(user1);
        booking1.setStatus(BookingStatus.APPROVED);

        assertThatThrownBy(() -> bookingService.updateBooking(100L, true, user1.getId())).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void updateBooking_alreadyApproved_ExceptionThrown_Test() {
        booking1.setItem(item1);
        booking1.setBooker(user1);
        item1.setOwner(user1);
        booking1.setStatus(BookingStatus.APPROVED);

        assertThatThrownBy(() -> bookingService.updateBooking(booking1.getId(), true, user1.getId())).isInstanceOf(BookingStatusAlreadyApprovedException.class);
    }

    /*  BookingDto findBookingById(Long bookingId, Long userId) - просмотр бронирования по id
    Может быть выполнено по существующему бронированию
    Может быть выполнено автором бронирования
    Может быть выполнено владельцем бронируемой вещи*/

    @Test
    public void findBookingById_byBooker_succeed_test() {
        booking1.setItem(item1);
        booking1.setBooker(user1);
        item1.setOwner(user2);

        assertThat(bookingService.findBookingById(booking1.getId(), user1.getId())).isEqualTo(BookingMapper.toDto(booking1));
    }

    @Test
    public void findBookingById_byItemOwner_succeed_test() {
        booking1.setItem(item1);
        booking1.setBooker(user2);
        item1.setOwner(user1);

        assertThat(bookingService.findBookingById(booking1.getId(), user1.getId())).isEqualTo(BookingMapper.toDto(booking1));
    }

    @Test
    public void findBookingById_notBookerAndNotItemOwner_exceptionThrown_test() {
        booking1.setItem(item1);
        booking1.setBooker(user1);
        item1.setOwner(user2);

        assertThatThrownBy(() -> bookingService.findBookingById(booking1.getId(), user3.getId())).isInstanceOf(IllegalAccessToEntityException.class);
    }

    /*List<BookingDto> findAllBookingsByOwnerIdAndState(Long bookerId, String state, int from, int size) - получение бронирований пользователя
    Бронирования должны возвращаться по дате от более новых к старым.
    * */

    @Test
    public void findAllBookingsByOwnerIdAndStateTest_currentBookings_succeed() {
        booking1.setStart(LocalDateTime.now().minusSeconds(10));
        booking1.setBooker(user1);
        booking1.setItem(item1);
        item1.setOwner(user3);
        booking2.setStart(LocalDateTime.now().minusSeconds(5));
        booking2.setBooker(user1);
        booking2.setItem(item2);
        item2.setOwner(user3);
        booking3.setStart(LocalDateTime.now().plusSeconds(10));
        booking3.setBooker(user1);
        booking3.setItem(item3);
        item3.setOwner(user3);

        assertThat(bookingService.findAllBookingsByOwnerIdAndState(user1.getId(), "CURRENT", 0, 20))
                .hasSize(2)
                .contains(BookingMapper.toDto(booking1))
                .contains(BookingMapper.toDto(booking2))
                .element(0).isEqualTo(BookingMapper.toDto(booking1));
    }

    @Test
    public void findAllBookingsByOwnerIdAndStateTest_pastBookings_succeed() {
        booking1.setEnd(LocalDateTime.now().minusSeconds(10));
        booking1.setBooker(user1);
        booking1.setItem(item1);
        item1.setOwner(user3);
        booking2.setEnd(LocalDateTime.now().minusSeconds(5));
        booking2.setBooker(user1);
        booking2.setItem(item2);
        item2.setOwner(user3);
        booking3.setStart(LocalDateTime.now().minusSeconds(10));
        booking3.setBooker(user1);
        booking3.setItem(item3);
        item3.setOwner(user3);

        assertThat(bookingService.findAllBookingsByOwnerIdAndState(user1.getId(), "PAST", 0, 20))
                .hasSize(2)
                .contains(BookingMapper.toDto(booking1))
                .contains(BookingMapper.toDto(booking2));
    }

    @Test
    public void findAllBookingsByOwnerIdAndStateTest_notExistingUser_exceptionThrown() {
        booking1.setEnd(LocalDateTime.now().minusSeconds(10));
        booking1.setBooker(user1);
        booking1.setItem(item1);
        item1.setOwner(user3);
        booking2.setEnd(LocalDateTime.now().minusSeconds(5));
        booking2.setBooker(user1);
        booking2.setItem(item2);
        item2.setOwner(user3);
        booking3.setStart(LocalDateTime.now().minusSeconds(10));
        booking3.setBooker(user1);
        booking3.setItem(item3);
        item3.setOwner(user3);

        assertThatThrownBy(() -> bookingService.findAllBookingsByOwnerIdAndState(100L, "PAST", 0, 20))
                .isInstanceOf(EntityNotFoundException.class);
    }

    /*List<BookingDto> findAllBookingsForOwnerItemsWithState(Long ownerId, String state, int from, int size) - поиск бронирований вещей пользователя
     * Запрос имеет смысл для владельца хотя бы одной вещи. */

    @Test
    public void findAllBookingsForOwnerItemsWithStateTest_currentBookings_succeed() {
        booking1.setStart(LocalDateTime.now().minusSeconds(10));
        booking1.setBooker(user1);
        booking1.setItem(item1);
        item1.setOwner(user3);
        booking2.setStart(LocalDateTime.now().minusSeconds(5));
        booking2.setBooker(user1);
        booking2.setItem(item2);
        item2.setOwner(user3);
        booking3.setStart(LocalDateTime.now().plusSeconds(10));
        booking3.setBooker(user1);
        booking3.setItem(item3);
        item3.setOwner(user3);

        assertThat(bookingService.findAllBookingsForOwnerItemsWithState(user3.getId(), "CURRENT", 0, 20))
                .hasSize(2)
                .contains(BookingMapper.toDto(booking1))
                .contains(BookingMapper.toDto(booking2))
                .element(0).isEqualTo(BookingMapper.toDto(booking1));
    }

    @Test
    public void findAllBookingsForOwnerItemsWithStateTest_pastBookings_succeed() {
        booking1.setStart(LocalDateTime.now().minusSeconds(20));
        booking1.setEnd(LocalDateTime.now().minusSeconds(10));
        booking1.setBooker(user1);
        booking1.setItem(item1);
        item1.setOwner(user3);
        booking2.setStart(LocalDateTime.now().minusSeconds(10));
        booking2.setEnd(LocalDateTime.now().minusSeconds(5));
        booking2.setBooker(user1);
        booking2.setItem(item2);
        item2.setOwner(user3);
        booking3.setStart(LocalDateTime.now().plusSeconds(10));
        booking3.setBooker(user1);
        booking3.setItem(item3);
        item3.setOwner(user3);

        assertThat(bookingService.findAllBookingsForOwnerItemsWithState(user3.getId(), "PAST", 0, 20))
                .hasSize(2)
                .contains(BookingMapper.toDto(booking1))
                .contains(BookingMapper.toDto(booking2))
                .element(0).isEqualTo(BookingMapper.toDto(booking1));
    }

    @Test
    public void findAllBookingsForOwnerItemsWithStateTest_notExistingUser_exceptionThrown() {
        booking1.setStart(LocalDateTime.now().minusSeconds(10));
        booking1.setBooker(user1);
        booking1.setItem(item1);
        item1.setOwner(user3);
        booking2.setStart(LocalDateTime.now().minusSeconds(5));
        booking2.setBooker(user1);
        booking2.setItem(item2);
        item2.setOwner(user3);
        booking3.setStart(LocalDateTime.now().plusSeconds(10));
        booking3.setBooker(user1);
        booking3.setItem(item3);
        item3.setOwner(user3);

        assertThatThrownBy(() -> bookingService.findAllBookingsByOwnerIdAndState(100L, "CURRENT", 0, 20))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private Booking copyOf(Booking booking) {
        return Booking.builder()
                .id(booking.getId())
                .item(booking.getItem())
                .status(booking.getStatus())
                .booker(booking.getBooker())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }
}