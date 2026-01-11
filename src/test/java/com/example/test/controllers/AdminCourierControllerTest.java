package com.example.test.controllers;

import com.example.test.controllers.courier.AdminCourierController;
import com.example.test.models.dtos.courierDto.CourierDto;
import com.example.test.models.entities.user.User;
import com.example.test.security.JwtAuthenticationFilter;
import com.example.test.services.courierService.CourierApplicationService;
import com.example.test.services.courierService.CourierAssignmentService;
import com.example.test.services.courierService.CourierService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCourierController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminCourierControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean
    private CourierApplicationService courierApplicationService;

    @MockBean
    private CourierAssignmentService courierAssignmentService;

    @MockBean
    private CourierService courierService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean(name = "courierSecurity")
    private Object courierSecurity;

    private UserDetailsImpl adminDetails;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        User admin = User.builder().id(99L).email("admin@test.com").build();
        adminDetails = new UserDetailsImpl(admin);
        auth = new UsernamePasswordAuthenticationToken(adminDetails, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("POST /approve/{id} - Success")
    void approve_Success() throws Exception {
        mockMvc.perform(post("/api/v1/couriers/approve/1")
                        .principal(auth)
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Courier application approved successfully"));

        verify(courierApplicationService).approve(1L, 99L);
    }

    @Test
    @DisplayName("POST /reject/{id} - Success with comment")
    void reject_Success() throws Exception {
        String comment = "Documents are invalid";

        mockMvc.perform(post("/api/v1/couriers/reject/1")
                        .content(comment)
                        .contentType(MediaType.TEXT_PLAIN)
                        .principal(auth)
                        .with(user(adminDetails)))
                .andExpect(status().isOk());

        verify(courierApplicationService).reject(1L, 99L, comment);
    }

    @Test
    @DisplayName("PATCH /{id}/assign - Success")
    void assignOrder_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/couriers/1/assign")
                        .param("orderId", "500")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order assigned to courier successfully"));

        verify(courierAssignmentService).assignOrderToCourier(1L, 500L);
    }

    @Test
    @DisplayName("GET /{id} - Returns CourierDto")
    void getCourier_Success() throws Exception {
        CourierDto dto = new CourierDto();
        dto.setId(1L);
        given(courierService.getCourier(1L)).willReturn(dto);

        mockMvc.perform(get("/api/v1/couriers/1")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE /{id} - Success")
    void delete_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/couriers/1")
                        .principal(auth))
                .andExpect(status().isOk());

        verify(courierService).deleteCourier(1L);
    }
}
