//package com.siemens.internship;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(controllers = ItemController.class)
//@Import(GlobalExceptionHandler.class)
//class GlobalExceptionHandlerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private ItemService itemService;
//
//    @DisplayName("When name is blank or too short/long, validation error handled by advice")
//    @Test
//    void validationErrorIsHandledByControllerAdvice() throws Exception {
//        // Test blank name
//        mockMvc.perform(post("/api/items")
//                        .contentType("application/json")
//                        .content("{\"name\":\"\",\"description\":\"x\",\"email\":\"not-an-email\",\"status\":\"NEW\"}"))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.name").value("Name cannot be blank"))
//                .andExpect(jsonPath("$.email").value("Invalid email format"));
//
//        // Test too short name
//        mockMvc.perform(post("/api/items")
//                        .contentType("application/json")
//                        .content("{\"name\":\"A\",\"description\":\"x\",\"email\":\"test@domain.com\",\"status\":\"NEW\"}"))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.name").value("Name must be between 2 and 50 characters"));
//    }
//}
package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void whenValidationFails_thenReturnsBadRequest() throws Exception {
        String body = "{\"name\":\"\",\"description\":\"d\",\"email\":\"invalid\",\"status\":null}";
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name cannot be blank"))
                .andExpect(jsonPath("$.email").value("Invalid email format"));
    }

    @Test
    void whenNotFound_thenReturnsNotFound() throws Exception {
        when(itemService.findById(999L)).thenThrow(new EntityNotFoundException("Item not found"));
        mockMvc.perform(get("/api/items/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Item not found"));
    }

    @Test
    void whenUnhandledException_thenReturnsInternalServerError() throws Exception {
        when(itemService.findById(anyLong())).thenThrow(new RuntimeException("Unexpected"));
        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("An unexpected error occurred"));
    }
}