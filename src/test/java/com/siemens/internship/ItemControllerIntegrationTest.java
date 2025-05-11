package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class ItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository repo;

    @Autowired
    private ObjectMapper om;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
        repo.saveAll(List.of(
                new Item(null, "Alpha", "desc1", "NEW",  "a@x.com"),
                new Item(null, "Beta",  "desc2", "DONE", "b@y.com")
        ));
    }

    @Test
    void getAllItems() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getItemById_found() throws Exception {
        Long id = repo.findAll().get(0).getId();
        mockMvc.perform(get("/api/items/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name", is("Alpha")));
    }

    @Test
    void getItemById_notFound() throws Exception {
        mockMvc.perform(get("/api/items/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));  // empty body on 404
    }

    @Test
    void createItem_valid() throws Exception {
        Item newItem = new Item(null, "Gamma", "desc3", "NEW", "c@z.com");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(newItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Gamma"));
    }

    @Test
    void createItem_invalid() throws Exception {
        // name too short + invalid email
        Map<String,Object> bad = Map.of(
                "name", "X",
                "description", "d",
                "status", "NEW",
                "email","invalid"
        );

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", is("Name must be between 2 and 50 characters")))
                .andExpect(jsonPath("$.email", is("Invalid email format")));
    }

    @Test
    void updateItem_success() throws Exception {
        Item existing = repo.findAll().get(0);
        existing.setName("Alpha-Updated");

        mockMvc.perform(put("/api/items/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(existing)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alpha-Updated"));
    }

    @Test
    void updateItem_notFound() throws Exception {
        Map<String,Object> payload = Map.of(
                "name",        "ZZ",
                "description", "z",
                "status",      "NEW",
                "email",       "z@z.com"
        );

        mockMvc.perform(put("/api/items/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(payload)))
                .andExpect(status().isNotFound())
                // now assert the actual response body
                .andExpect(content().string("Item not found"));
    }

    @Test
    void updateItem_validationFailure() throws Exception {
        Item existing = repo.findAll().get(0);
        existing.setName("X");  // too short

        mockMvc.perform(put("/api/items/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(existing)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0]", containsString("Name must be between")));
    }

    @Test
    void deleteItem_success() throws Exception {
        Long id = repo.findAll().get(0).getId();
        mockMvc.perform(delete("/api/items/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteItem_notFound() throws Exception {
        mockMvc.perform(delete("/api/items/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));  // empty body
    }

    @Test
    void processItems_endpointReturnsList() throws Exception {
        // first, fire the async request
        MvcResult mvc = mockMvc.perform(get("/api/items/process"))
                .andExpect(request().asyncStarted())
                .andReturn();

        // then dispatch and assert on its body
        mockMvc.perform(asyncDispatch(mvc))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].status", is("PROCESSED")));
    }
}
