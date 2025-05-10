package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service layer containing business logic for Items.
 * Handles CRUD operations and asynchronous batch processing.
 */
@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    /**
     * Thread pool for parallel async processing.
     * You can tune the pool size as needed.
     */
    private final ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * Asynchronously processes *every* item by:
     *  1. Retrieving them from the DB
     *  2. Setting status to "PROCESSED"
     *  3. Saving back to the DB
     *
     * Tracks which ones succeeded and returns that list once *all* are done.
     *
     * @return a CompletableFuture that completes with the list of processed items
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        // 1) fetch all items
        List<Item> toProcess = itemRepository.findAll();

        // thread-safe trackers
        AtomicInteger successCount = new AtomicInteger(0);
        ConcurrentLinkedQueue<Item> processedItems = new ConcurrentLinkedQueue<>();

        // 2) kick off one CompletableFuture per item
        List<CompletableFuture<Void>> futures = toProcess.stream()
                .map(item -> CompletableFuture.runAsync(() -> {
                    try {
                        // update and save
                        item.setStatus("PROCESSED");
                        Item saved = itemRepository.save(item);

                        // record success
                        processedItems.add(saved);
                        successCount.incrementAndGet();
                    } catch (Exception ex) {
                        // log the failure and skip this item
                        // e.g. logger.error("Failed to process item {}", item.getId(), ex);
                    }
                }, executor))
                .collect(Collectors.toList());

        // 3) when *all* are done, return the list of successes
        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(ignored -> {
                    // you could also expose successCount.get() if you like
                    return processedItems
                            .stream()
                            .collect(Collectors.toList());
                });
    }

    // --- the rest of your CRUD methods ---
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }
}
