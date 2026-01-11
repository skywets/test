package com.example.test.controllers;

import com.example.test.controllers.cart.CartController;
import com.example.test.models.dtos.cartDto.CartDto;
import com.example.test.models.entities.user.User;
import com.example.test.security.JwtAuthenticationFilter;
import com.example.test.services.cartService.CartService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    private UserDetailsImpl userDetails;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        User user = User.builder().id(1L).email("test@test.com").build();
        userDetails = new UserDetailsImpl(user);
        auth = new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptyList());

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .defaultRequest(post("/**").with(csrf()))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/cart/items - Успех")
    void addItem_Success() throws Exception {
        given(cartService.addItem(eq(1L), anyLong(), anyInt())).willReturn(new CartDto());

        mockMvc.perform(post("/api/v1/cart/items")
                        .param("menuItemId", "10")
                        .param("quantity", "1")
                        .with(user(userDetails))
                        .principal(auth))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /api/v1/cart - Успех")
    void getCart_Success() throws Exception {
        given(cartService.getCurrentCart(1L)).willReturn(new CartDto());

        mockMvc.perform(get("/api/v1/cart")
                        .principal(auth)
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/cart/items/{id} - Успех")
    void updateItem_Success() throws Exception {
        given(cartService.updateItem(eq(1L), anyLong(), anyInt())).willReturn(new CartDto());

        mockMvc.perform(put("/api/v1/cart/items/1")
                        .param("quantity", "5")
                        .principal(auth)
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/cart/items/{id} - Успех")
    void removeItem_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/items/1")
                        .principal(auth)
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Item removed from cart"));
    }

    @Test
    @DisplayName("DELETE /api/v1/cart - Успех")
    void clearCart_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/cart")
                        .principal(auth)
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cart cleared successfully"));
    }

    @Test
    @DisplayName("POST /api/v1/cart/items - Ошибка валидации (quantity=0)")
    void addItem_ValidationFail() throws Exception {
        mockMvc.perform(post("/api/v1/cart/items")
                        .param("menuItemId", "10")
                        .param("quantity", "0")
                        .principal(auth)
                        .with(user(userDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"));
    }

}
