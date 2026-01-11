package com.example.test.controllers;

import com.example.test.controllers.user.AuthenticationController;
import com.example.test.models.dtos.userDto.LoginRequest;
import com.example.test.models.dtos.userDto.UserRegisterDto;
import com.example.test.models.entities.user.User;
import com.example.test.security.JwtAuthenticationFilter;
import com.example.test.security.JwtService;
import com.example.test.services.userService.impl.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("POST /api/v1/auth/login - Success login")
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest("user@test.com", "password123");
        User mockUser = User.builder().id(1L).email("user@test.com").build();

        given(authenticationService.authenticate(any(LoginRequest.class))).willReturn(mockUser);
        given(jwtService.generateToken(any())).willReturn("mock-jwt-token");
        given(jwtService.getExpirationTime()).willReturn(3600000L);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup - Success registration")
    void signup_Success() throws Exception {
        UserRegisterDto dto = UserRegisterDto.builder()
                .name("Test User")
                .email("newuser@test.com")
                .password("password123")
                .build();

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(dto.getEmail());

        given(authenticationService.signup(any(UserRegisterDto.class)))
                .willReturn(mockUser);

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

}