package com.siemens.internship;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void testSaveAndFindById() {
        // save a single item and verify you can fetch it by ID
        Item item = new Item(null, "RepoTest", "Desc", "NEW", "repo@test.com");
        Item saved = itemRepository.save(item);

        Optional<Item> fetched = itemRepository.findById(saved.getId());
        assertTrue(fetched.isPresent(), "Item should be present after save");
        assertEquals("RepoTest", fetched.get().getName());
    }

    @Test
    void testFindAllIds() {
        // save a couple of items, then verify the custom findAllIds query
        Item a = itemRepository.save(new Item(null, "One", "d1", "NEW", "one@test.com"));
        Item b = itemRepository.save(new Item(null, "Two", "d2", "NEW", "two@test.com"));

        List<Long> ids = itemRepository.findAllIds();
        assertTrue(ids.contains(a.getId()));
        assertTrue(ids.contains(b.getId()));
        assertEquals(2, ids.size(), "Should return exactly two IDs");
    }
}
