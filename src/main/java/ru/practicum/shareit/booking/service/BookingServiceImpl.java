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

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    private final UserRepositoryJPA userRepository;
    private final ItemRepositoryJPA itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    public Booking createBooking(BookingDto bookingDto, Long userId) {
        Booking booking = bookingMapper.toBooking(bookingDto);
        booking.setItem(itemRepository.findById(bookingDto.getItemId()).orElseThrow(() -> new EntityNotFoundException("User not found.", userId, String.valueOf(Thread.currentThread().getStackTrace()[1]))));
        booking.setBooker(userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found.", userId, String.valueOf(Thread.currentThread().getStackTrace()[1]))));
        validateBookerNotOwner(booking, userId);
        booking.setStatus(BookingStatus.WAITING);
        return BookingMapper.initializeBooking(bookingRepository.save(booking));
    }

    private void validateBookerNotOwner(Booking booking, Long bookerId) {
        if (booking.getItem().getOwner().getId().equals(bookerId)) {
            throw new IllegalAccessToEntityException("Fail to grant access to book item by owner.", booking.getItem().getId(), bookerId, String.valueOf(Thread.currentThread().getStackTrace()[1]));
        }
    }

    @Override
    public Booking updateBooking(Long bookingId, boolean approved, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new EntityNotFoundException("Booking not found.", bookingId, String.valueOf(Thread.currentThread().getStackTrace()[1])));
        validateBookerOwner(booking, ownerId);
        validateNotApprovedBooking(booking);
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.initializeBooking(bookingRepository.save(booking));
    }

    @Override
    public Booking findBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new EntityNotFoundException("Booking not found.", bookingId, String.valueOf(Thread.currentThread().getStackTrace()[1])));
        validateBookingAccess(booking, userId);
        return BookingMapper.initializeBooking(booking);
    }

    @Override
    public List<Booking> findAllBookingsByOwnerIdAndState(Long bookerId, String state) {
        validateUser(bookerId);
        switch (state) {
            case "CURRENT":
                return bookingRepository.findAllCurrentBookingsByOwnerIdAndState(bookerId);
            case "PAST":
                return bookingRepository.findAllPastBookingsByOwnerIdAndState(bookerId);
            case "FUTURE":
                return bookingRepository.findAllFutureBookingsByOwnerIdAndState(bookerId);
            case "WAITING":
                return bookingRepository.findAllWaitingBookingsByOwnerIdAndState(bookerId);
            case "REJECTED":
                return bookingRepository.findAllRejectedBookingsByOwnerIdAndState(bookerId);
            case "ALL":
                return bookingRepository.findAllBookingsByOwnerIdAndState(bookerId);
        }
        throw new UnknownStateException(String.format("Unknown state: %s", state), state, String.valueOf(Thread.currentThread().getStackTrace()[1]));
    }

    @Override
    public List<Booking> findAllBookingsForOwnerItemsWithState(Long ownerId, String state) {
        validateUser(ownerId);
        switch (state) {
            case "CURRENT":
                return bookingRepository.findAllCurrentBookingsForOwnerItems(ownerId);
            case "PAST":
                return bookingRepository.findAllPastBookingsForOwnerItems(ownerId);
            case "FUTURE":
                return bookingRepository.findAllFutureBookingsForOwnerItems(ownerId);
            case "WAITING":
                return bookingRepository.findAllWaitingBookingsForOwnerItems(ownerId);
            case "REJECTED":
                return bookingRepository.findAllRejectedBookingsForOwnerItems(ownerId);
            case "ALL":
                return bookingRepository.findAllBookingsForOwnerItems(ownerId);
        }
        throw new UnknownStateException(String.format("Unknown state: %s", state), state, String.valueOf(Thread.currentThread().getStackTrace()[1]));
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
}