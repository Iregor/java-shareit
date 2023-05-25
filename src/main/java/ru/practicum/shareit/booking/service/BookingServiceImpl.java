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
import ru.practicum.shareit.user.repository.UserRepositoryJPA;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    private final UserRepositoryJPA userRepository;
    private final BookingMapper bookingMapper;

    @Override
    public Booking createBooking(BookingDto bookingDto, Long userId) {
        Booking booking = bookingMapper.toBooking(bookingDto, userId);
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
    public List<Booking> findAllBookingsByOwnerIdAndState(Long ownerId, String state) {
        validateUser(ownerId);
        return bookingRepository.findAllBookingsByOwnerIdAndState(ownerId, getStatusCondition(state)).stream().map(BookingMapper::initializeBooking).collect(Collectors.toList());

    }

    @Override
    public List<Booking> findAllBookingsForOwnerItemsWithState(Long ownerId, String state) {
        validateUser(ownerId);
        return bookingRepository.findAllBookingsForOwnerItemsWithState(ownerId, getStatusCondition(state));
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

    private String getStatusCondition(String state) {
        String statusCondition = "";
        switch (state) {
            case "CURRENT":
                statusCondition = " AND current_timestamp BETWEEN b.start AND b.end";
                break;
            case "PAST":
                statusCondition = " AND current_timestamp > b.end";
                break;
            case "FUTURE":
                statusCondition = " AND current_timestamp < b.start";
                break;
            case "WAITING":
                statusCondition = " AND b.status = 'WAITING'";
                break;
            case "REJECTED":
                statusCondition = " AND b.status = 'REJECTED'";
                break;
            case "ALL":
                break;
            default:
                throw new UnknownStateException(String.format("Unknown state: %s", state), state, String.valueOf(Thread.currentThread().getStackTrace()[1]));
        }
        return statusCondition;
    }

    private void validateNotApprovedBooking(Booking booking) {
        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new BookingStatusAlreadyApprovedException("Booking status has been already approved.", booking.getId(), String.valueOf(Thread.currentThread().getStackTrace()[1]));
        }
    }
}