package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepositoryJPA extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(Long id);

    List<Item> findAllByAvailable(boolean available);

    List<Item> findAllByDescriptionContainingIgnoreCaseAndAvailable(String partialDescription, boolean available);
}
