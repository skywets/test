package com.example.test.controllers;

import com.example.test.controllers.order.OrderController;
import com.example.test.exceptions.GlobalExceptionHandler;
import com.example.test.models.dtos.orderDto.OrderDto;
import com.example.test.models.entities.enums.OrderStatus;
import com.example.test.models.entities.enums.PaymentMethod;
import com.example.test.models.entities.user.RoleT;
import com.example.test.models.entities.user.User;
import com.example.test.security.JwtAuthenticationFilter;
import com.example.test.services.orderService.OrderService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    private UserDetailsImpl userDetails;
    private UsernamePasswordAuthenticationToken userAuth;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        RoleT userRole = RoleT.builder().name("USER").permissionTSet(new HashSet<>()).build();
        User user = User.builder().id(1L).email("user@test.com").roleTSet(Set.of(userRole)).build();
        userDetails = new UserDetailsImpl(user);
        userAuth = new UsernamePasswordAuthenticationToken(userDetails, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .alwaysDo(print())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/orders - Success creation")
    void createOrder_Success() throws Exception {
        OrderDto responseDto = new OrderDto();
        given(orderService.createOrderFromCart(eq(1L), eq(10L), eq(PaymentMethod.CARD)))
                .willReturn(responseDto);

        mockMvc.perform(post("/api/v1/orders")
                        .param("restaurantId", "10")
                        .param("paymentMethod", "CARD")
                        .with(csrf())
                        .with(user(userDetails))
                        .principal(userAuth))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /api/v1/orders/my - Get current user orders")
    void getMyOrders_Success() throws Exception {
        given(orderService.getUserOrders(1L)).willReturn(List.of(new OrderDto()));

        mockMvc.perform(get("/api/v1/orders/my")
                        .with(user(userDetails))
                        .principal(userAuth))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /status - Success update for ADMIN")
    void updateStatus_AdminSuccess() throws Exception {
        OrderDto responseDto = new OrderDto();
        given(orderService.updateStatus(any(), eq(100L), eq(OrderStatus.CONFIRMED)))
                .willReturn(responseDto);

        RoleT adminRole = RoleT.builder().name("ADMIN").permissionTSet(new HashSet<>()).build();
        User adminEntity = User.builder().id(2L).roleTSet(Set.of(adminRole)).build();
        UserDetailsImpl adminDetails = new UserDetailsImpl(adminEntity);

        mockMvc.perform(patch("/api/v1/orders/100/status")
                        .param("status", "CONFIRMED")
                        .with(csrf())
                        .with(user(adminDetails)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /status - 403 Forbidden for simple USER")
    void updateStatus_UserForbidden() throws Exception {
        given(orderService.updateStatus(any(), anyLong(), any()))
                .willThrow(new org.springframework.security.access.AccessDeniedException("Access denied"));

        mockMvc.perform(patch("/api/v1/orders/100/status")
                        .param("status", "DELIVERED")
                        .with(csrf())
                        .with(user(userDetails))
                        .principal(userAuth))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Access denied"));
    }

    @Test
    @DisplayName("PATCH /status - 403 for COURIER if status is not DELIVERED")
    void updateStatus_CourierInvalidStatus() throws Exception {
        RoleT courierRole = RoleT.builder().name("COURIER").permissionTSet(new HashSet<>()).build();
        User courierEntity = User.builder().id(2L).roleTSet(Set.of(courierRole)).build();
        UserDetailsImpl courierDetails = new UserDetailsImpl(courierEntity);

        given(orderService.updateStatus(any(), anyLong(), eq(OrderStatus.CONFIRMED)))
                .willThrow(new org.springframework.security.access.AccessDeniedException("Courier can only set DELIVERED status"));

        mockMvc.perform(patch("/api/v1/orders/100/status")
                        .param("status", "CONFIRMED")
                        .with(csrf())
                        .with(user(courierDetails)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Courier can only set DELIVERED status"));
    }

    @Test
    @DisplayName("DELETE /api/v1/orders/{id} - Success cancellation")
    void cancelOrder_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/orders/100")
                        .with(csrf())
                        .with(user(userDetails))
                        .principal(userAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"));
    }

    @Test
    @DisplayName("POST /api/v1/orders - Validation fail (negative restaurant ID)")
    void createOrder_ValidationFail() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .param("restaurantId", "-5")
                        .param("paymentMethod", "CARD")
                        .with(csrf())
                        .with(user(userDetails))
                        .principal(userAuth))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"));
    }
}
