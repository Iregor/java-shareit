package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    List<Booking> findAllByBookerId(Long bookerId, Pageable page);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime now1, LocalDateTime now2, Pageable page);

    List<Booking> findAllByBookerIdAndEndBefore(Long bookerId, LocalDateTime now, Pageable page);

    List<Booking> findAllByBookerIdAndStartAfter(Long bookerId, LocalDateTime now, Pageable page);

    List<Booking> findAllByBookerIdAndStatus(Long bookerId, BookingStatus status, Pageable page);

    List<Booking> findAllByItemOwnerId(Long ownerId, Pageable page);

    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime now1, LocalDateTime now2, Pageable page);

    List<Booking> findAllByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime now, Pageable page);

    List<Booking> findAllByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime now, Pageable page);

    List<Booking> findAllByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Pageable page);
}
