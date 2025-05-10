package com.siemens.internship;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void testSaveAndFindById() {
        Item item = new Item(null, "RepoTest", "Desc", "NEW", "repo@test.com");
        Item saved = itemRepository.save(item);

        Optional<Item> fetched = itemRepository.findById(saved.getId());
        assertTrue(fetched.isPresent());
        assertEquals("RepoTest", fetched.get().getName());
    }
}

