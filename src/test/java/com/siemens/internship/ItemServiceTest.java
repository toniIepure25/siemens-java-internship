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
    void testProcessItemsAsync_processesAllAndFiltersNulls() throws Exception {
        // given
        when(itemRepository.findAllIds()).thenReturn(List.of(1L, 2L, 3L));

        Item i1 = new Item(1L, "A", "d", "NEW", "a@x.com");
        Item i2 = new Item(2L, "B", "d", "NEW", "b@x.com");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(i1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(i2));
        when(itemRepository.findById(3L)).thenReturn(Optional.empty());

        // saving returns the same instance
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> processed = future.get(5, TimeUnit.SECONDS);

        // then
        assertEquals(2, processed.size(), "Should process exactly 2 items");
        assertTrue(processed.stream().allMatch(it -> "PROCESSED".equals(it.getStatus())));
        verify(itemRepository, times(2)).save(any(Item.class));
    }
}
