package com.example.test.controllers;

import com.example.test.controllers.user.UserAdminController;
import com.example.test.models.dtos.userDto.UserHistoryDto;
import com.example.test.security.JwtAuthenticationFilter;
import com.example.test.services.userService.UserHistoryService;
import com.example.test.services.userService.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserHistoryService userHistoryService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/v1/users/filter - Success")
    void getUsers_ReturnsPage() throws Exception {
        given(userService.findAllByFilter(any(), any())).willReturn(Page.empty());

        mockMvc.perform(get("/api/v1/users/filter")
                        .param("role", "USER")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - Validation Failure (Negative ID)")
    void getById_InvalidId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/users/-5"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/users/{id}/deactivate - Success")
    void deactivate_ValidId_ReturnsOk() throws Exception {
        willDoNothing().given(userService).deactivate(anyLong());

        mockMvc.perform(post("/api/v1/users/1/deactivate"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id}/history - Success")
    void history_ValidParams_ReturnsDto() throws Exception {
        LocalDate testDate = LocalDate.now();
        given(userHistoryService.getUserStateAt(anyLong(), any(LocalDate.class)))
                .willReturn(new UserHistoryDto());

        mockMvc.perform(get("/api/v1/users/1/history")
                        .param("date", testDate.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id}/history - Validation Failure (Future Date)")
    void history_FutureDate_ReturnsBadRequest() throws Exception {
        String futureDate = LocalDate.now().plusDays(1).toString();

        mockMvc.perform(get("/api/v1/users/1/history")
                        .param("date", futureDate))
                .andExpect(status().isBadRequest());
    }
}
