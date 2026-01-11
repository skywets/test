package com.example.test.controllers;

import com.example.test.controllers.paymentController.PaymentController;
import com.example.test.exceptions.GlobalExceptionHandler;
import com.example.test.models.dtos.payment.CreatePaymentDto;
import com.example.test.models.dtos.payment.PaymentDto;
import com.example.test.models.dtos.payment.UpdatePaymentStatusDto;
import com.example.test.models.entities.enums.PaymentStatus;
import com.example.test.models.entities.user.RoleT;
import com.example.test.models.entities.user.User;
import com.example.test.security.JwtAuthenticationFilter;
import com.example.test.services.paymentService.PaymentService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class)
@EnableMethodSecurity
class PaymentControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    private UserDetailsImpl userDetails;
    private UsernamePasswordAuthenticationToken userAuth;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        RoleT userRole = RoleT.builder().name("USER").permissionTSet(new HashSet<>()).build();
        User user = User.builder().id(1L).email("user@test.com").roleTSet(Set.of(userRole)).active(true).build();
        userDetails = new UserDetailsImpl(user);
        userAuth = new UsernamePasswordAuthenticationToken(userDetails, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .alwaysDo(print())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/payments - Success creation")
    void create_Success() throws Exception {
        CreatePaymentDto dto = CreatePaymentDto.builder()
                .orderId(100L)
                .amount(new BigDecimal("500.00"))
                .build();
        given(paymentService.createPayment(any(), eq(1L))).willReturn(new PaymentDto());

        mockMvc.perform(post("/api/v1/payments")
                        .with(csrf())
                        .with(user(userDetails))
                        .principal(userAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /api/v1/payments/{id} - Success retrieval")
    void getById_Success() throws Exception {
        given(paymentService.getPaymentStatus(eq(100L), eq(1L))).willReturn(new PaymentDto());

        mockMvc.perform(get("/api/v1/payments/100")
                        .with(user(userDetails))
                        .principal(userAuth))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /status - Forbidden for USER role")
    void updateStatus_ForbiddenForUser() throws Exception {
        UpdatePaymentStatusDto dto = new UpdatePaymentStatusDto();
        dto.setStatus(PaymentStatus.PAID);

        mockMvc.perform(patch("/api/v1/payments/100/status")
                        .with(csrf())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /status - Success for ADMIN role")
    void updateStatus_AdminSuccess() throws Exception {
        RoleT adminRole = RoleT.builder().name("ADMIN").permissionTSet(new HashSet<>()).build();
        User adminUser = User.builder().id(2L).roleTSet(Set.of(adminRole)).build();
        UserDetailsImpl adminDetails = new UserDetailsImpl(adminUser);

        UpdatePaymentStatusDto dto = new UpdatePaymentStatusDto();
        dto.setStatus(PaymentStatus.PAID);

        given(paymentService.updatePaymentStatus(eq(100L), any())).willReturn(new PaymentDto());

        mockMvc.perform(patch("/api/v1/payments/100/status")
                        .with(csrf())
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }
}

