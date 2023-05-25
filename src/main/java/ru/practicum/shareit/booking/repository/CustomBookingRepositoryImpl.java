package ru.practicum.shareit.booking.repository;

import ru.practicum.shareit.booking.model.Booking;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class CustomBookingRepositoryImpl implements CustomBookingRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<Booking> findAllBookingsByOwnerIdAndState(Long bookerId, String statusCondition) {
        return em.createQuery("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item it JOIN FETCH it.owner WHERE b.booker.id = :bookerId" + statusCondition + " ORDER BY b.start DESC").setParameter("bookerId", bookerId).getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Booking> findAllBookingsForOwnerItemsWithState(Long ownerId, String statusCondition) {
        return em.createQuery("SELECT b FROM Booking b JOIN FETCH b.booker JOIN FETCH b.item it JOIN FETCH it.owner own WHERE own.id = :ownerId" + statusCondition + " ORDER BY b.start DESC").setParameter("ownerId", ownerId).getResultList();
    }
}
