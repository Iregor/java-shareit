package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.exceptions.BookingStatusAlreadyApprovedException;
import ru.practicum.shareit.exception.exceptions.EntityNotFoundException;
import ru.practicum.shareit.exception.exceptions.IllegalAccessToEntityException;
import ru.practicum.shareit.exception.exceptions.UnknownStateException;
import ru.practicum.shareit.item.repository.ItemRepositoryJPA;
import ru.practicum.shareit.user.repository.UserRepositoryJPA;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepositoryJPA userRepository;
    private final ItemRepositoryJPA itemRepository;

    @Override
    public BookingDto createBooking(BookingDto bookingDto, Long userId) {
        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setItem(itemRepository.findById(bookingDto.getItemId()).orElseThrow(() -> new EntityNotFoundException("Item not found.", userId, String.valueOf(Thread.currentThread().getStackTrace()[1]))));
        booking.setBooker(userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found.", userId, String.valueOf(Thread.currentThread().getStackTrace()[1]))));
        validateBookerNotOwner(booking, userId);
        booking.setStatus(BookingStatus.WAITING);
        return toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto updateBooking(Long bookingId, boolean approved, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new EntityNotFoundException("Booking not found.", bookingId, String.valueOf(Thread.currentThread().getStackTrace()[1])));
        validateBookerOwner(booking, ownerId);
        validateNotApprovedBooking(booking);
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto findBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new EntityNotFoundException("Booking not found.", bookingId, String.valueOf(Thread.currentThread().getStackTrace()[1])));
        validateBookingAccess(booking, userId);
        return toBookingDto(BookingMapper.initializeBooking(booking));
    }

    @Override
    public List<BookingDto> findAllBookingsByOwnerIdAndState(Long bookerId, String state) {
        validateUser(bookerId);
        switch (state) {
            case "CURRENT":
                return bookingRepository.findAllCurrentBookingsByOwnerId(bookerId).stream().map(this::toBookingDto).collect(Collectors.toList());
            case "PAST":
                return bookingRepository.findAllPastBookingsByOwnerId(bookerId).stream().map(this::toBookingDto).collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.findAllFutureBookingsByOwnerId(bookerId).stream().map(this::toBookingDto).collect(Collectors.toList());
            case "WAITING":
                return bookingRepository.findAllWaitingBookingsByOwnerId(bookerId).stream().map(this::toBookingDto).collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository.findAllRejectedBookingsByOwnerId(bookerId).stream().map(this::toBookingDto).collect(Collectors.toList());
            case "ALL":
                return bookingRepository.findAllBookingsByOwnerIdAndState(bookerId).stream().map(this::toBookingDto).collect(Collectors.toList());
        }
        throw new UnknownStateException(String.format("Unknown state: %s", state), state, String.valueOf(Thread.currentThread().getStackTrace()[1]));
    }

    @Override
    public List<BookingDto> findAllBookingsForOwnerItemsWithState(Long ownerId, String state) {
        validateUser(ownerId);
        switch (state) {
            case "CURRENT":
                return bookingRepository.findAllCurrentBookingsForOwnerItems(ownerId).stream().map(this::toBookingDto).collect(Collectors.toList());
            case "PAST":
                return bookingRepository.findAllPastBookingsForOwnerItems(ownerId).stream().map(this::toBookingDto).collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.findAllFutureBookingsForOwnerItems(ownerId).stream().map(this::toBookingDto).collect(Collectors.toList());
            case "WAITING":
                return bookingRepository.findAllWaitingBookingsForOwnerItems(ownerId).stream().map(this::toBookingDto).collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository.findAllRejectedBookingsForOwnerItems(ownerId).stream().map(this::toBookingDto).collect(Collectors.toList());
            case "ALL":
                return bookingRepository.findAllBookingsForOwnerItems(ownerId).stream().map(this::toBookingDto).collect(Collectors.toList());
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

    private BookingDto toBookingDto(Booking booking) {
        BookingDto dto = BookingMapper.toDto(booking);
        dto.setBooker(new BookingDto.Booker(booking.getBooker().getId(), booking.getBooker().getName()));
        dto.setItem(new BookingDto.Item(booking.getItem().getId(), booking.getItem().getName()));
        return dto;
    }
}