package com.siemens.internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @BeforeEach
    void openMocks() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessItemsAsync_successfullyProcessesAllItems() throws Exception {
        // prepare two real items
        Item item1 = new Item(1L, "Item1", "desc", "NEW", "a@b.com");
        Item item2 = new Item(2L, "Item2", "desc", "NEW", "b@b.com");
        List<Item> toProcess = List.of(item1, item2);

        // stub repository behavior
        when(itemRepository.findAll()).thenReturn(toProcess);
        // echo back the saved item
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        // invoke the async processing
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        // wait up to 5 seconds for completion
        List<Item> processed = future.get(5, TimeUnit.SECONDS);

        // verify results
        assertEquals(2, processed.size(), "Should process exactly 2 items");
        // each returned item must have status set to PROCESSED
        processed.forEach(it ->
                assertEquals("PROCESSED", it.getStatus(), "Each item should be marked PROCESSED")
        );
        // verify save was called twice
        verify(itemRepository, times(2)).save(any(Item.class));
    }
}
