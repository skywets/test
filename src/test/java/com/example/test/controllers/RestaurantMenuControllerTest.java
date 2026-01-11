package com.example.test.controllers;

import com.example.test.controllers.restaurantMenuController.RestaurantMenuController;
import com.example.test.exceptions.GlobalExceptionHandler;
import com.example.test.models.dtos.cartDto.MenuItemDto;
import com.example.test.models.dtos.resMenuDto.UpdateMenuAvailabilityDto;
import com.example.test.models.entities.cuisine.CuisineType;
import com.example.test.models.entities.user.RoleT;
import com.example.test.models.entities.user.User;
import com.example.test.security.JwtAuthenticationFilter;
import com.example.test.services.resMenuService.RestaurantMenuService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestaurantMenuController.class)
@Import(GlobalExceptionHandler.class)
@EnableMethodSecurity
class RestaurantMenuControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestaurantMenuService restaurantMenuService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    private UserDetailsImpl userDetails;
    private UsernamePasswordAuthenticationToken ownerAuth;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        RoleT ownerRole = RoleT.builder().name("RESTAURANT_OWNER").permissionTSet(new HashSet<>()).build();
        User ownerUser = User.builder().id(10L).email("owner@test.com").roleTSet(Set.of(ownerRole)).active(true).build();
        userDetails = new UserDetailsImpl(ownerUser);
        ownerAuth = new UsernamePasswordAuthenticationToken(userDetails, null, List.of(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER")));

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .alwaysDo(print())
                .build();
    }

    @Test
    @DisplayName("POST /menu - Success addition")
    void addMenuItem_Success() throws Exception {
        MenuItemDto dto = MenuItemDto.builder()
                .name("Pizza")
                .price(new BigDecimal("12.99"))
                .quantity(1)
                .cuisineType(CuisineType.ITALIAN)
                .build();
        given(restaurantMenuService.addMenuItem(eq(1L), any(), eq(10L))).willReturn(dto);

        mockMvc.perform(post("/api/v1/restaurants/1/menu")
                        .with(csrf())
                        .with(user(userDetails))
                        .principal(ownerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /menu - Success with authenticated user")
    void getMenu_AuthenticatedSuccess() throws Exception {
        given(restaurantMenuService.getMenu(1L)).willReturn(List.of(new MenuItemDto()));

        mockMvc.perform(get("/api/v1/restaurants/1/menu")
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /menu/{id} - Success update")
    void updateMenuItem_Success() throws Exception {
        MenuItemDto dto = MenuItemDto.builder()
                .name("Pizza")
                .price(new BigDecimal("12.99"))
                .quantity(1)
                .cuisineType(CuisineType.ITALIAN)
                .build();
        given(restaurantMenuService.updateMenuItem(eq(1L), eq(50L), any(), eq(10L))).willReturn(dto);

        mockMvc.perform(put("/api/v1/restaurants/1/menu/50")
                        .with(csrf())
                        .with(user(userDetails))
                        .principal(ownerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /menu/{id} - Success removal")
    void removeMenuItem_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/restaurants/1/menu/50")
                        .with(csrf())
                        .with(user(userDetails))
                        .principal(ownerAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Menu item removed successfully"));
    }

    @Test
    @DisplayName("PATCH /availability - Success update")
    void updateAvailability_Success() throws Exception {
        UpdateMenuAvailabilityDto dto = new UpdateMenuAvailabilityDto();
        dto.setAvailable(false);

        MenuItemDto response = MenuItemDto.builder().available(false).build();
        given(restaurantMenuService.updateAvailability(eq(1L), eq(50L), eq(false), eq(10L))).willReturn(response);

        mockMvc.perform(patch("/api/v1/restaurants/1/menu/50/availability")
                        .with(csrf())
                        .with(user(userDetails))
                        .principal(ownerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /menu - Forbidden for simple USER")
    void addMenuItem_Forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/restaurants/1/menu")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
