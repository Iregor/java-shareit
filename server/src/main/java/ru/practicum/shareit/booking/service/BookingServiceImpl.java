package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.exceptions.BookingStatusAlreadyApprovedException;
import ru.practicum.shareit.exception.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exception.exceptions.IllegalAccessToEntityException;
import ru.practicum.shareit.exception.exceptions.UnknownStateException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDto createBooking(BookingDto bookingDto, Long userId) {
        Booking booking = BookingMapper.toBooking(bookingDto);
        Item item;
        booking.setItem(item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(() -> new EntityNotFoundException("Item not found.", bookingDto.getItemId(), String.valueOf(Thread.currentThread().getStackTrace()[1]))));
        assertItemAvailable(item);
        booking.setBooker(userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found.", userId, String.valueOf(Thread.currentThread().getStackTrace()[1]))));
        validateBookerNotOwner(booking, userId);
        booking.setStatus(BookingStatus.WAITING);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto updateBooking(Long bookingId, boolean approved, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new EntityNotFoundException("Booking not found.", bookingId, String.valueOf(Thread.currentThread().getStackTrace()[1])));
        validateBookerOwner(booking, ownerId);
        validateNotApprovedBooking(booking);
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto findBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new EntityNotFoundException("Booking not found.", bookingId, String.valueOf(Thread.currentThread().getStackTrace()[1])));
        validateBookingAccess(booking, userId);
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> findAllBookingsByOwnerIdAndState(Long bookerId, String state, int from, int size) {
        validateUser(bookerId);
        Pageable page = PageRequest.of(from / size, size, Sort.by("start").descending());
        switch (state) {
            case "CURRENT":
                return bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(bookerId, LocalDateTime.now(), LocalDateTime.now(), page).stream().map(BookingMapper::toDto).collect(Collectors.toList());
            case "PAST":
                return bookingRepository.findAllByBookerIdAndEndBefore(bookerId, LocalDateTime.now(), page).stream().map(BookingMapper::toDto).collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.findAllByBookerIdAndStartAfter(bookerId, LocalDateTime.now(), page).stream().map(BookingMapper::toDto).collect(Collectors.toList());
            case "WAITING":
                return bookingRepository.findAllByBookerIdAndStatus(bookerId, BookingStatus.WAITING, page).stream().map(BookingMapper::toDto).collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository.findAllByBookerIdAndStatus(bookerId, BookingStatus.REJECTED, page).stream().map(BookingMapper::toDto).collect(Collectors.toList());
            case "ALL":
                return bookingRepository.findAllByBookerId(bookerId, page).stream().map(BookingMapper::toDto).collect(Collectors.toList());
        }
        throw new UnknownStateException(String.format("Unknown state: %s", state), state, String.valueOf(Thread.currentThread().getStackTrace()[1]));
    }

    @Override
    public List<BookingDto> findAllBookingsForOwnerItemsWithState(Long ownerId, String state, int from, int size) {
        validateUser(ownerId);
        Pageable page = PageRequest.of(from / size, size, Sort.by("start").descending());
        switch (state) {
            case "CURRENT":
                return bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfter(ownerId, LocalDateTime.now(), LocalDateTime.now(), page).stream().map(BookingMapper::toDto).collect(Collectors.toList());
            case "PAST":
                return bookingRepository.findAllByItemOwnerIdAndEndBefore(ownerId, LocalDateTime.now(), page).stream().map(BookingMapper::toDto).collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.findAllByItemOwnerIdAndStartAfter(ownerId, LocalDateTime.now(), page).stream().map(BookingMapper::toDto).collect(Collectors.toList());
            case "WAITING":
                return bookingRepository.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING, page).stream().map(BookingMapper::toDto).collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, page).stream().map(BookingMapper::toDto).collect(Collectors.toList());
            case "ALL":
                return bookingRepository.findAllByItemOwnerId(ownerId, page).stream().map(BookingMapper::toDto).collect(Collectors.toList());
        }
        throw new UnknownStateException(String.format("Unknown state: %s", state), state, String.valueOf(Thread.currentThread().getStackTrace()[1]));
    }

    private void validateBookerNotOwner(Booking booking, Long bookerId) {
        if (booking.getItem().getOwner().getId().equals(bookerId)) {
            throw new IllegalAccessToEntityException("Fail to grant access to book item by owner.", booking.getItem().getId(), bookerId, String.valueOf(Thread.currentThread().getStackTrace()[1]));
        }
    }

    private void validateBookerOwner(Booking booking, Long ownerId) {
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new IllegalAccessToEntityException(String.format("Fail to grant access to booking id: %d to user id: %d", booking.getId(), ownerId), booking.getId(), ownerId, String.valueOf(Thread.currentThread().getStackTrace()[1]));
        }
    }

    private void validateBookingAccess(Booking booking, Long userId) {
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new IllegalAccessToEntityException("Fail grant access to booking.", booking.getId(), userId, String.valueOf(Thread.currentThread().getStackTrace()[1]));
        }
    }

    private void validateUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found.", userId, String.valueOf(Thread.currentThread().getStackTrace()[1]));
        }
    }

    private void validateNotApprovedBooking(Booking booking) {
        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new BookingStatusAlreadyApprovedException("Booking status has been already approved.", booking.getId(), String.valueOf(Thread.currentThread().getStackTrace()[1]));
        }
    }

    private void assertItemAvailable(Item item) {
        if (!item.getAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}