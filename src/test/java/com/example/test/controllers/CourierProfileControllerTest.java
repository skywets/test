package com.example.test.controllers;

import com.example.test.controllers.courier.CourierProfileController;
import com.example.test.models.dtos.courierDto.CourierDto;
import com.example.test.models.dtos.courierDto.CourierStatusDto;
import com.example.test.models.dtos.courierDto.CourierVehicleDto;
import com.example.test.models.entities.enums.VehicleType;
import com.example.test.models.entities.user.User;
import com.example.test.security.JwtAuthenticationFilter;
import com.example.test.services.courierService.CourierService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourierProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class CourierProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private CourierService courierService;


    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    private UserDetailsImpl courierDetails;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(5L).email("courier@test.com").build();
        courierDetails = new UserDetailsImpl(user);
        auth = new UsernamePasswordAuthenticationToken(courierDetails, null,
                List.of(new SimpleGrantedAuthority("ROLE_COURIER")));
    }

    @Test
    @DisplayName("PATCH /vehicle - Success")
    void updateVehicle_Success() throws Exception {
        CourierVehicleDto dto = new CourierVehicleDto();
        dto.setVehicleType(VehicleType.BIKE);

        given(courierService.updateVehicle(eq(5L), any())).willReturn(new CourierDto());

        mockMvc.perform(patch("/api/v1/courier/profile/vehicle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .principal(auth)
                        .with(user(courierDetails)))
                .andExpect(status().isOk());

        verify(courierService).updateVehicle(5L, VehicleType.BIKE);
    }

    @Test
    @DisplayName("PATCH /status - Success")
    void updateStatus_Success() throws Exception {
        CourierStatusDto dto = new CourierStatusDto();
        dto.setStatus("ONLINE");

        given(courierService.updateStatus(eq(5L), any())).willReturn(new CourierDto());

        mockMvc.perform(patch("/api/v1/courier/profile/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .principal(auth)
                        .with(user(courierDetails)))
                .andExpect(status().isOk());

        verify(courierService).updateStatus(5L, "ONLINE");
    }

    @Test
    @DisplayName("PATCH /status - Validation Fail (Null Status)")
    void updateStatus_ValidationFail() throws Exception {
        CourierStatusDto invalidDto = new CourierStatusDto();

        mockMvc.perform(patch("/api/v1/courier/profile/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .principal(auth)
                        .with(user(courierDetails)))
                .andExpect(status().isBadRequest());
    }
}
