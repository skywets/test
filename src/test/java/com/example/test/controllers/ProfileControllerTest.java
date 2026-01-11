package com.example.test.controllers;

import com.example.test.controllers.user.ProfileController;
import com.example.test.models.dtos.userDto.UpdatePasswordDto;
import com.example.test.models.dtos.userDto.UpdateProfileDto;
import com.example.test.models.dtos.userDto.UserDto;
import com.example.test.models.entities.user.User;
import com.example.test.security.JwtAuthenticationFilter;
import com.example.test.services.userService.UserProfileService;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserProfileService userProfileService;

    @MockBean
    private JwtAuthenticationFilter jwtFilter;
    @MockBean
    private UserDetailsService userDetailsService;

    private UserDetailsImpl mockUserDetails;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");
        mockUserDetails = new UserDetailsImpl(user);

        auth = new UsernamePasswordAuthenticationToken(
                mockUserDetails,
                null,
                Collections.emptyList()
        );
    }
    @Test
    @DisplayName("GET /api/v1/profile - Success")
    void getProfile_Success() throws Exception {
        given(userProfileService.getCurrentProfile("test@example.com")).willReturn(new UserDto());

        mockMvc.perform(get("/api/v1/profile")
                        .with(authentication(auth))
                        .principal(auth))
                .andDo(print())
                .andExpect(status().isOk());
    }





    @Test
    @DisplayName("PUT /api/v1/profile - Success")
    void updateProfile_Success() throws Exception {
        UpdateProfileDto dto = UpdateProfileDto.builder()
                .name("Updated Name")
                .email("test@example.com")
                .build();

        willDoNothing().given(userProfileService).updateProfile(eq(1L), any(UpdateProfileDto.class));

        mockMvc.perform(put("/api/v1/profile")
                        .with(csrf())
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile updated successfully"));
    }

    @Test
    @DisplayName("PUT /api/v1/profile/password - Success")
    void updatePassword_Success() throws Exception {
        UpdatePasswordDto dto = UpdatePasswordDto.builder()
                .oldPassword("oldPass")
                .newPassword("newPass")
                .build();

        willDoNothing().given(userProfileService).updatePassword(eq(1L), any(UpdatePasswordDto.class));

        mockMvc.perform(put("/api/v1/profile/password")
                        .with(csrf())
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully"));
    }

}
