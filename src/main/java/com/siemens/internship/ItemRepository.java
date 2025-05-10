package com.siemens.internship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Spring Data JPA repository for Item entities.
 * Provides built-in CRUD operations.
 */
public interface ItemRepository extends JpaRepository<Item, Long> {
    /**
     * Custom query to fetch all item IDs.
     * Used in asynchronous processing.
     */
    @Query("SELECT id FROM Item")
    List<Long> findAllIds();
}
