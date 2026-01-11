package com.example.test.controllers;

import com.example.test.controllers.notificationController.NotificationController;
import com.example.test.exceptions.GlobalExceptionHandler;
import com.example.test.models.dtos.notificationDto.CreateNotificationDto;
import com.example.test.models.dtos.notificationDto.NotificationDto;
import com.example.test.models.dtos.notificationDto.UpdateNotificationStatusDto;
import com.example.test.models.entities.user.RoleT;
import com.example.test.models.entities.user.User;
import com.example.test.security.JwtAuthenticationFilter;
import com.example.test.security.NotificationSecurity;
import com.example.test.services.notiService.NotificationService;
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

@WebMvcTest(NotificationController.class)
@Import(GlobalExceptionHandler.class)
@EnableMethodSecurity
class NotificationControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean(name = "notificationSecurity")
    private NotificationSecurity notificationSecurity;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    private UserDetailsImpl userDetails;
    private UsernamePasswordAuthenticationToken userAuth;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        RoleT userRole = RoleT.builder().name("USER").permissionTSet(new HashSet<>()).build();
        User user = User.builder()
                .id(1L)
                .email("user@test.com")
                .roleTSet(Set.of(userRole))
                .active(true)
                .build();
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
    @DisplayName("POST /api/v1/notifications - Should return 403 Forbidden for simple USER")
    void create_ForbiddenForUser() throws Exception {
        CreateNotificationDto dto = new CreateNotificationDto();
        dto.setUserId(1L);
        dto.setMessage("System Alert");

        mockMvc.perform(post("/api/v1/notifications")
                        .with(csrf())
                        .with(user(userDetails))
                        .principal(userAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /status - Should return 403 Forbidden when USER tries to set FAILED status")
    void updateStatus_ForbiddenForFailedStatus() throws Exception {
        UpdateNotificationStatusDto dto = new UpdateNotificationStatusDto();
        dto.setStatus("FAILED");

        given(notificationSecurity.isOwner(eq(10L), any())).willReturn(true);

        mockMvc.perform(patch("/api/v1/notifications/10/status")
                        .with(csrf())
                        .with(user(userDetails))
                        .principal(userAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Users are only allowed to mark notifications as READ"));
    }

    @Test
    @DisplayName("PATCH /status - Should return 200 OK when owner sets READ status")
    void updateStatus_SuccessRead() throws Exception {
        given(notificationSecurity.isOwner(eq(10L), any())).willReturn(true);

        String json = "{\"status\":\"READ\"}";

        mockMvc.perform(patch("/api/v1/notifications/10/status")
                        .with(csrf())
                        .with(user(userDetails))
                        .principal(userAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification status updated to READ"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications - Should return 200 OK when retrieving user notifications")
    void getMyNotifications_Success() throws Exception {
        given(notificationService.getByUser(1L)).willReturn(List.of(new NotificationDto()));

        mockMvc.perform(get("/api/v1/notifications")
                        .with(user(userDetails))
                        .principal(userAuth))
                .andExpect(status().isOk());
    }
}
