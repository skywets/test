package com.example.test.controllers;

import com.example.test.controllers.cart.MenuItemController;
import com.example.test.models.dtos.cartDto.MenuItemDto;
import com.example.test.models.entities.cuisine.CuisineType;
import com.example.test.security.JwtAuthenticationFilter;
import com.example.test.services.cartService.MenuItemService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MenuItemController.class)
@AutoConfigureMockMvc(addFilters = false)
class MenuItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MenuItemService menuItemService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    private MenuItemDto validDto;

    @BeforeEach
    void setUp() {
        validDto = MenuItemDto.builder()
                .name("Pizza")
                .price(new BigDecimal("500.00"))
                .available(true)
                .quantity(10)
                .cuisineType(CuisineType.ITALIAN)
                .foodTypeIds(List.of(1L))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/menu-items - Success creation")
    void create_Success() throws Exception {
        given(menuItemService.create(any(MenuItemDto.class))).willReturn(validDto);

        mockMvc.perform(post("/api/v1/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Pizza"));
    }

    @Test
    @DisplayName("GET /api/v1/menu-items/{id} - Success retrieval")
    void getById_Success() throws Exception {
        given(menuItemService.getById(1L)).willReturn(validDto);

        mockMvc.perform(get("/api/v1/menu-items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pizza"));
    }

    @Test
    @DisplayName("PUT /api/v1/menu-items/{id} - Validation error for negative ID")
    void update_InvalidId_BadRequest() throws Exception {
        mockMvc.perform(put("/api/v1/menu-items/-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"));
    }

    @Test
    @DisplayName("DELETE /api/v1/menu-items/{id} - Success deletion")
    void delete_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/menu-items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value( "Menu item with ID %d has been deleted successfully".formatted(1)));
    }

    @Test
    @DisplayName("POST /api/v1/menu-items - @Valid error in RequestBody")
    void create_InvalidDto_BadRequest() throws Exception {
        MenuItemDto invalidDto = MenuItemDto.builder().name("").build();

        mockMvc.perform(post("/api/v1/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}
