package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exception.exceptions.IllegalAccessToEntityException;
import ru.practicum.shareit.exception.exceptions.ItemIdNotConsistentException;
import ru.practicum.shareit.exception.exceptions.NoResolvedBookingException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final RequestRepository requestRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        User owner = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found.", userId, String.valueOf(Thread.currentThread().getStackTrace()[1])));
        Request request;
        if (itemDto.getRequestId() != null) {
            request = requestRepository.findById(itemDto.getRequestId()).orElseThrow(() -> new EntityNotFoundException("Request not found.", itemDto.getRequestId(), String.valueOf(Thread.currentThread().getStackTrace()[1])));
        } else {
            request = null;
        }
        return ItemMapper.toItemDto(itemRepository.save(ItemMapper.toItem(itemDto, owner, request)));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long userId, Long itemId) throws IllegalAccessException {
        validateUserAccess(userId, itemId);
        validateItemIdConsistency(itemDto, userId, itemId);
        Item itemToSave = ItemMapper.toItem(itemDto);
        itemToSave.setOwner(userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found.", userId, String.valueOf(Thread.currentThread().getStackTrace()[1]))));
        buildItemEntity(itemToSave, itemId);
        return ItemMapper.toItemDto(itemRepository.save(itemToSave));
    }

    @Override
    public ItemDto findItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("Item not found.", itemId, String.valueOf(Thread.currentThread().getStackTrace()[1])));
        ItemWithBookingsDto dto = ItemMapper.toItemWithBookingsDto(item);
        dto.setComments(commentRepository.findAllByItemIdOrderById(itemId).stream().map(CommentMapper::toCommentDto).collect(Collectors.toList()));
        return userId.equals(item.getOwner().getId()) ? setBookingBookerIdDto(dto) : dto;
    }

    @Override
    public List<ItemDto> findOwnerItems(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found.", userId, String.valueOf(Thread.currentThread().getStackTrace()[1]));
        }
        return itemRepository.findAllByOwnerIdOrderByIdAsc(userId)
                .stream()
                .map(ItemMapper::toItemWithBookingsDto)
                .map(this::setBookingBookerIdDto)
                .map(this::setCommentsToDto)
                .sorted(Comparator.comparingLong(ItemDto::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchAvailableItems(String text) {
        return itemRepository.findAllByDescriptionContainingIgnoreCaseAndAvailable(text, true).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public CommentDto createComment(CommentDto commentDto, Long itemId, Long userId) {
        validateResolvedBooking(itemId, userId);
        Comment comment = buildCommentEntity(commentDto, itemId, userId);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private void validateResolvedBooking(Long itemId, Long userId) {
        bookingRepository.findFirstByItemIdAndBookerIdAndStatusAndEndBefore(itemId, userId, BookingStatus.APPROVED, LocalDateTime.now()).orElseThrow(() -> new NoResolvedBookingException("No resolved booking found.", itemId, userId, String.valueOf(Thread.currentThread().getStackTrace()[1])));
    }

    private Comment buildCommentEntity(CommentDto commentDto, Long itemId, Long userId) {
        return Comment.builder()
                .item(itemRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("Item not found.", itemId, String.valueOf(Thread.currentThread().getStackTrace()[1]))))
                .author(userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found.", userId, String.valueOf(Thread.currentThread().getStackTrace()[1]))))
                .text(commentDto.getText())
                .created(commentDto.getCreated() == null ? LocalDateTime.now() : commentDto.getCreated())
                .build();
    }

    private void validateUserAccess(Long userId, Long itemId) {
        Item existedItem = itemRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("User not found.", itemId, String.valueOf(Thread.currentThread().getStackTrace()[1])));
        if (!existedItem.getOwner().getId().equals(userId)) {
            throw new IllegalAccessToEntityException(String.format("Fail to grant access user id: %s to item id: %s", userId, itemId), itemId, userId, String.valueOf(Thread.currentThread().getStackTrace()[1]));
        }
    }

    private void validateItemIdConsistency(ItemDto itemDto, Long userId, Long itemId) {
        if (itemDto.getId() != null && !itemDto.getId().equals(itemId)) {
            throw new ItemIdNotConsistentException(LocalDateTime.now() + " : " + Thread.currentThread().getStackTrace()[1], String.format("Fail to validate id consistensy for dto: %s and itemId: %s", itemDto, itemId), itemDto, userId, itemId);
        }
    }

    private ItemWithBookingsDto setBookingBookerIdDto(ItemWithBookingsDto dto) {
        dto.setLastBooking(BookingMapper.toBookingBookerIdDto(bookingRepository.findFirstByItemIdAndStartBeforeAndStatusOrderByEndDesc(dto.getId(), LocalDateTime.now(), BookingStatus.APPROVED).orElse(null)));
        dto.setNextBooking(BookingMapper.toBookingBookerIdDto(bookingRepository.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(dto.getId(), LocalDateTime.now(), BookingStatus.APPROVED).orElse(null)));
        return dto;
    }

    private ItemWithBookingsDto setCommentsToDto(ItemWithBookingsDto dto) {
        dto.setComments(commentRepository.findAllByItemIdOrderById(dto.getId()).stream().map(CommentMapper::toCommentDto).collect(Collectors.toList()));
        return dto;
    }

    private void buildItemEntity(Item itemToSave, Long itemId) throws IllegalAccessException {
        Item itemRepo = itemRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("Item not found.", itemId, String.valueOf(Thread.currentThread().getStackTrace()[1])));
        Class<? extends Item> clss = itemRepo.getClass();
        for (Field field : clss.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.get(itemToSave) == null) {
                field.set(itemToSave, field.get(itemRepo));
            }
        }
    }
}
