package com.example.test.controllers;

import com.example.test.controllers.courier.CourierApplicationController;
import com.example.test.models.dtos.courierDto.CourierApplicationDto;
import com.example.test.models.entities.user.User;
import com.example.test.security.JwtAuthenticationFilter;
import com.example.test.services.courierService.CourierApplicationService;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourierApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class CourierApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private CourierApplicationService courierApplicationService;


    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    private UserDetailsImpl userDetails;
    private UsernamePasswordAuthenticationToken auth;
    private CourierApplicationDto validDto;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(1L).email("test@test.com").build();
        userDetails = new UserDetailsImpl(user);
        auth = new UsernamePasswordAuthenticationToken(userDetails, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        validDto = new CourierApplicationDto();
        validDto.setDocuments("license.pdf, id_card.pdf");
    }

    @Test
    @DisplayName("POST /api/v1/applications/couriers - Success")
    void createApplication_Success() throws Exception {
        given(courierApplicationService.createApplication(any(CourierApplicationDto.class))).willReturn(validDto);

        mockMvc.perform(post("/api/v1/applications/couriers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto))
                        .principal(auth)
                        .with(user(userDetails)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documents").value("license.pdf, id_card.pdf"));

        verify(courierApplicationService).createApplication(any(CourierApplicationDto.class));
    }

    @Test
    @DisplayName("GET /api/v1/applications/couriers/me - Success")
    void getMyApplications_Success() throws Exception {
        given(courierApplicationService.getByUser(1L)).willReturn(List.of(validDto));

        mockMvc.perform(get("/api/v1/applications/couriers/me")
                        .principal(auth)
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].documents").value("license.pdf, id_card.pdf"));

        verify(courierApplicationService).getByUser(1L);
    }
}
