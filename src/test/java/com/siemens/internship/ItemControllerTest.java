package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllItems_returnsList() throws Exception {
        List<Item> items = List.of(
                new Item(1L, "Test", "Desc", "NEW", "test@mail.com")
        );
        when(itemService.findAll()).thenReturn(items);

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test"));
    }

    @Test
    void testGetItemById_found() throws Exception {
        Item item = new Item(1L, "Found", "Desc", "NEW", "found@mail.com");
        when(itemService.findById(1L)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("found@mail.com"));
    }

    @Test
    void testGetItemById_notFound() throws Exception {
        when(itemService.findById(100L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/100"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void testCreateItem_valid() throws Exception {
        Item input = new Item(null, "New", "Desc", "NEW", "new@mail.com");
        Item saved = new Item(42L, "New", "Desc", "NEW", "new@mail.com");
        when(itemService.save(any(Item.class))).thenReturn(saved);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void testCreateItem_invalidEmail() throws Exception {
        Item bad = new Item(null, "Bad", "Desc", "NEW", "not-an-email");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Invalid email format"));
    }
}
