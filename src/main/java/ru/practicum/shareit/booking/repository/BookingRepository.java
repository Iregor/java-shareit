package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findFirstByItemIdAndStartBeforeAndStatusOrderByEndDesc(Long itemId, LocalDateTime currentTime, BookingStatus status);

    Optional<Booking> findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(Long itemId, LocalDateTime currentTime, BookingStatus status);

    Optional<Booking> findFirstByItemIdAndBookerIdAndStatusAndEndBefore(Long itemId, Long bookerId, BookingStatus status, LocalDateTime time);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item it JOIN FETCH it.owner WHERE b.booker.id = :bookerId ORDER BY b.start DESC")
    List<Booking> findAllBookingsByOwnerIdAndState(Long bookerId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item it JOIN FETCH it.owner WHERE b.booker.id = :bookerId AND current_timestamp BETWEEN b.start AND b.end ORDER BY b.start DESC")
    List<Booking> findAllCurrentBookingsByOwnerIdAndState(Long bookerId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item it JOIN FETCH it.owner WHERE b.booker.id = :bookerId AND current_timestamp > b.end ORDER BY b.start DESC")
    List<Booking> findAllPastBookingsByOwnerIdAndState(Long bookerId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item it JOIN FETCH it.owner WHERE b.booker.id = :bookerId AND current_timestamp < b.start ORDER BY b.start DESC")
    List<Booking> findAllFutureBookingsByOwnerIdAndState(Long bookerId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item it JOIN FETCH it.owner WHERE b.booker.id = :bookerId AND b.status = 'WAITING' ORDER BY b.start DESC")
    List<Booking> findAllWaitingBookingsByOwnerIdAndState(Long bookerId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item it JOIN FETCH it.owner WHERE b.booker.id = :bookerId AND b.status = 'REJECTED' ORDER BY b.start DESC")
    List<Booking> findAllRejectedBookingsByOwnerIdAndState(Long bookerId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item it JOIN FETCH it.owner own WHERE own.id = :ownerId ORDER BY b.start DESC")
    List<Booking> findAllBookingsForOwnerItems(Long ownerId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item it JOIN FETCH it.owner own WHERE own.id = :ownerId AND current_timestamp BETWEEN b.start AND b.end ORDER BY b.start DESC")
    List<Booking> findAllCurrentBookingsForOwnerItems(Long ownerId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item it JOIN FETCH it.owner own WHERE own.id = :ownerId AND current_timestamp > b.end ORDER BY b.start DESC")
    List<Booking> findAllPastBookingsForOwnerItems(Long ownerId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item it JOIN FETCH it.owner own WHERE own.id = :ownerId AND current_timestamp < b.start ORDER BY b.start DESC")
    List<Booking> findAllFutureBookingsForOwnerItems(Long ownerId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item it JOIN FETCH it.owner own WHERE own.id = :ownerId AND b.status = 'WAITING' ORDER BY b.start DESC")
    List<Booking> findAllWaitingBookingsForOwnerItems(Long ownerId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item it JOIN FETCH it.owner own WHERE own.id = :ownerId AND b.status = 'REJECTED' ORDER BY b.start DESC")
    List<Booking> findAllRejectedBookingsForOwnerItems(Long ownerId);
}
