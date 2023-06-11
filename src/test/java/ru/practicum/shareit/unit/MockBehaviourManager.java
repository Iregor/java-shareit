package ru.practicum.shareit.unit;

import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;

//идея мок-репозиториев - доставать сущности из hashMap по id, или присваивать id при сохранении т.е. имитирует идею БД
public class MockBehaviourManager {
    public static void setUserRepositoryBehaviour(UserRepository userRepository, Map<Long, User> users) {

        Mockito.lenient().when(userRepository.existsById(Mockito.anyLong())).thenAnswer(invocation -> {
            long userId = invocation.getArgument(0, Long.class);
            return users.containsKey(userId);
        });

        Mockito.lenient().when(userRepository.findById(Mockito.anyLong()))
                .thenAnswer(invocation -> {
                    long id = invocation.getArgument(0, Long.class);
                    return Optional.ofNullable(users.get(id));
                });

        Mockito.lenient().when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0, User.class);
                    if (user.getId() == null) {

                        user.setId(users.keySet().stream().max(Comparator.comparingLong(key -> key)).orElse(0L) + 1);
                    }
                    return user;
                });

        Mockito.lenient().when(userRepository.findAll())
                .thenReturn(new ArrayList<>(users.values()));
    }

    public static void setItemRepositoryBehaviour(ItemRepository itemRepository, Map<Long, Item> items) {

        Mockito.lenient().when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> {
                    Item item = invocation.getArgument(0, Item.class);
                    if (item.getId() == null) {
                        item.setId(1L);
                    }
                    return item;
                });

        Mockito.lenient().when(itemRepository.findById(Mockito.anyLong())).thenAnswer(invocation -> {
            long id = invocation.getArgument(0, Long.class);
            return Optional.ofNullable(items.get(id));
        });

        Mockito.lenient().when(itemRepository.findAllByDescriptionContainingIgnoreCaseAndAvailable(Mockito.anyString(), Mockito.anyBoolean()))
                .thenAnswer(invocation -> {
                    String desc = invocation.getArgument(0, String.class);
                    boolean available = invocation.getArgument(1, Boolean.class);
                    return items.values()
                            .stream()
                            .filter(item -> item.getDescription().contains(desc))
                            .filter(Item::getAvailable)
                            .sorted(Comparator.comparingLong(Item::getId))
                            .collect(Collectors.toList());
                });

        Mockito.lenient().when(itemRepository.findAllByItemRequestId(Mockito.anyLong()))
                .thenAnswer(invocation -> {
                    long requestId = invocation.getArgument(0, Long.class);
                    return items.values()
                            .stream()
                            .filter(item -> item.getItemRequest() != null)
                            .filter(item -> item.getItemRequest().getId().equals(requestId))
                            .collect(Collectors.toList());
                });
    }

    public static void setBookingRepositoryBehaviour(BookingRepository bookingRepository, Map<Long, Booking> bookings) {
        Mockito.lenient().when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> {
                    Booking booking = invocation.getArgument(0, Booking.class);
                    booking.setId(1L);
                    return booking;
                });

        Mockito.lenient().when(bookingRepository.findById(Mockito.anyLong())).thenAnswer(invocation -> {
            long id = invocation.getArgument(0, Long.class);
            return Optional.ofNullable(bookings.get(id));
        });

        Mockito.lenient().when(bookingRepository.findFirstByItemIdAndBookerIdAndStatusAndEndBefore(
                        Mockito.anyLong(), Mockito.anyLong(), any(BookingStatus.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    long itemId = invocation.getArgument(0, Long.class);
                    long userId = invocation.getArgument(1, Long.class);
                    return bookings.values()
                            .stream()
                            .filter(book -> book.getStatus().equals(BookingStatus.APPROVED))
                            .filter(book -> book.getEnd().isBefore(LocalDateTime.now()))
                            .filter(book -> book.getItem().getId() == itemId)
                            .filter(book -> book.getBooker().getId() == userId)
                            .findFirst();
                });

        Mockito.lenient().when(bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(Mockito.anyLong(), any(), any(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    long bookerId = invocation.getArgument(0, Long.class);
                    return bookings.values()
                            .stream()
                            .filter(b -> b.getBooker().getId() == bookerId)
                            .filter(b -> b.getStart().isBefore(LocalDateTime.now()) && b.getEnd().isAfter(LocalDateTime.now()))
                            .collect(Collectors.toList());
                });

        Mockito.lenient().when(bookingRepository.findAllByBookerIdAndEndBefore(Mockito.anyLong(), any(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    long bookerId = invocation.getArgument(0, Long.class);
                    return bookings.values()
                            .stream()
                            .filter(b -> b.getBooker().getId() == bookerId)
                            .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                            .collect(Collectors.toList());
                });

        Mockito.lenient().when(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfter(Mockito.anyLong(), any(), any(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    Long ownerId = invocation.getArgument(0, Long.class);
                    return bookings.values()
                            .stream()
                            .filter(b -> b.getItem().getOwner().getId().equals(ownerId))
                            .filter(b -> b.getStart().isBefore(LocalDateTime.now()))
                            .sorted((b1, b2) -> b1.getStart().isBefore(b2.getStart()) ? -1 : 1)
                            .collect(Collectors.toList());
                });

        Mockito.lenient().when(bookingRepository.findAllByItemOwnerIdAndEndBefore(Mockito.anyLong(), any(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    Long ownerId = invocation.getArgument(0, Long.class);
                    return bookings.values()
                            .stream()
                            .filter(b -> b.getItem().getOwner().getId().equals(ownerId))
                            .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                            .sorted((b1, b2) -> b1.getStart().isBefore(b2.getStart()) ? -1 : 1)
                            .collect(Collectors.toList());
                });
    }

    public static void setCommentRepositoryBehaviour(CommentRepository commentRepository, Map<Long, Comment> comments) {
        Mockito.lenient().when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> {
                    Comment comment = invocation.getArgument(0, Comment.class);
                    if (comment.getId() == null) {
                        comment.setId(1L);
                    }
                    return comment;
                });
    }

    public static void setRequestRepositoryBehaviour(RequestRepository requestRepository, Map<Long, Request> requests) {
        Mockito.lenient().when(requestRepository.findById(Mockito.anyLong())).thenAnswer(invocation -> {
            long id = invocation.getArgument(0, Long.class);
            return Optional.ofNullable(requests.get(id));
        });

        Mockito.lenient().when(requestRepository.save(any(Request.class)))
                .thenAnswer(invocation -> {
                    Request request = invocation.getArgument(0, Request.class);
                    if (request.getId() == null) {
                        request.setId(1L);
                    }
                    return request;
                });

        Mockito.lenient().when(requestRepository.findAllByRequestorIdOrderByCreatedDesc(Mockito.anyLong()))
                .thenAnswer(invocation -> {
                    Long requestorId = invocation.getArgument(0, Long.class);
                    return requests.values()
                            .stream()
                            .filter(request -> request.getRequestor() != null)
                            .filter(request -> request.getRequestor().getId().equals(requestorId))
                            .collect(Collectors.toList());
                });

        Mockito.lenient().when(requestRepository.findAllByRequestorIdNot(Mockito.anyLong(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    Long userId = invocation.getArgument(0, Long.class);
                    return requests.values().stream()
                            .filter(req -> req.getRequestor() != null)
                            .filter(req -> !req.getRequestor().getId().equals(userId))
                            .collect(Collectors.toList());
                });
    }
}
