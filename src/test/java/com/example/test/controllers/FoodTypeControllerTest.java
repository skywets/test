package com.example.test.controllers;

import com.example.test.controllers.foodTypeController.FoodTypeController;
import com.example.test.models.dtos.cuisine_foodTypeDto.FoodTypeDto;
import com.example.test.security.JwtAuthenticationFilter;
import com.example.test.services.foodTypeService.FoodTypeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FoodTypeController.class)
@AutoConfigureMockMvc(addFilters = false)
class FoodTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private FoodTypeService foodTypeService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    private FoodTypeDto validDto;

    @BeforeEach
    void setUp() {
        validDto = new FoodTypeDto();
        validDto.setId(1L);
        validDto.setName("Fast Food");
    }

    @Test
    @DisplayName("POST /api/v1/food-types - Success")
    void create_Success() throws Exception {
        given(foodTypeService.create(any(FoodTypeDto.class))).willReturn(validDto);

        mockMvc.perform(post("/api/v1/food-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Fast Food"));

        verify(foodTypeService).create(any(FoodTypeDto.class));
    }

    @Test
    @DisplayName("GET /api/v1/food-types/{id} - Success")
    void getById_Success() throws Exception {
        given(foodTypeService.getById(1L)).willReturn(validDto);

        mockMvc.perform(get("/api/v1/food-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fast Food"));
    }

    @Test
    @DisplayName("PUT /api/v1/food-types/{id} - Validation Fail (Negative ID)")
    void update_InvalidId_BadRequest() throws Exception {
        mockMvc.perform(put("/api/v1/food-types/-5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"));
    }

    @Test
    @DisplayName("DELETE /api/v1/food-types/{id} - Success")
    void delete_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/food-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Food type deleted successfully"));

        verify(foodTypeService).delete(1L);
    }

    @Test
    @DisplayName("GET /api/v1/food-types - Success Returns List")
    void getAll_Success() throws Exception {
        given(foodTypeService.getAll()).willReturn(List.of(validDto));

        mockMvc.perform(get("/api/v1/food-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Fast Food"))
                .andExpect(jsonPath("$.length()").value(1));
    }
}
