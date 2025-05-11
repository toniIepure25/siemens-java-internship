package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void whenValidationFails_thenReturnsBadRequest() throws Exception {
        Item invalid = new Item();
        invalid.setName("");               // triggers @NotBlank
        invalid.setEmail("not-an-email");  // triggers @Email

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                // now expect the @NotBlank message, not the size message
                .andExpect(jsonPath("$.name").value("Name cannot be blank"))
                .andExpect(jsonPath("$.email").value("Invalid email format"));
    }

    @Test
    void whenItemNotFound_thenReturns404() throws Exception {
        when(itemService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));  // still empty for this path
    }

    @Test
    void whenUnhandledException_thenReturnsInternalServerError() throws Exception {
        ItemService failingService = mock(ItemService.class);
        when(failingService.findById(1L)).thenThrow(new RuntimeException("Boom"));

        ItemController controller = new ItemController(failingService);
        MockMvc standalone = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        standalone.perform(get("/api/items/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Something went wrong: Boom"));
    }
}
