package com.example.test.controllers;

import com.example.test.controllers.restaurant.AdminRestaurantController;
import com.example.test.exceptions.GlobalExceptionHandler;
import com.example.test.models.dtos.restaurantDto.RestaurantDetailsDto;
import com.example.test.models.dtos.restaurantDto.RestaurantDto;
import com.example.test.models.entities.user.RoleT;
import com.example.test.models.entities.user.User;
import com.example.test.security.JwtAuthenticationFilter;
import com.example.test.services.resService.RestaurantApplicationService;
import com.example.test.services.resService.RestaurantService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminRestaurantController.class)
@Import(GlobalExceptionHandler.class)
@EnableMethodSecurity
class AdminRestaurantControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private RestaurantApplicationService applicationService;

    @MockBean
    private RestaurantService restaurantService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    private UserDetailsImpl adminDetails;
    private UsernamePasswordAuthenticationToken adminAuth;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        RoleT adminRole = RoleT.builder().name("ADMIN").permissionTSet(new HashSet<>()).build();
        User adminUser = User.builder().id(1L).email("admin@test.com").roleTSet(Set.of(adminRole)).active(true).build();
        adminDetails = new UserDetailsImpl(adminUser);
        adminAuth = new UsernamePasswordAuthenticationToken(adminDetails, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        RoleT userRole = RoleT.builder().name("USER").permissionTSet(new HashSet<>()).build();
        User normalUser = User.builder().id(2L).email("user@test.com").roleTSet(Set.of(userRole)).active(true).build();
        userDetails = new UserDetailsImpl(normalUser);

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .alwaysDo(print())
                .build();
    }

    @Test
    @DisplayName("POST /approve/{id} - Success for ADMIN")
    void approve_Success() throws Exception {
        mockMvc.perform(post("/api/v1/admin/restaurants/approve/10")
                        .with(csrf())
                        .with(user(adminDetails))
                        .principal(adminAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Restaurant application approved successfully"));
    }

    @Test
    @DisplayName("POST /reject/{id} - Success for ADMIN")
    void reject_Success() throws Exception {
        mockMvc.perform(post("/api/v1/admin/restaurants/reject/10")
                        .param("comment", "Incomplete documents")
                        .with(csrf())
                        .with(user(adminDetails))
                        .principal(adminAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Restaurant application rejected successfully"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/restaurants - Forbidden for USER")
    void getAll_ForbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/restaurants")
                        .with(user(userDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /{id} - Success for ADMIN")
    void delete_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/restaurants/10")
                        .with(csrf())
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Restaurant deleted successfully by admin"));
    }

    @Test
    @DisplayName("POST /reject/{id} - Validation fail (missing comment)")
    void reject_ValidationFail() throws Exception {
        mockMvc.perform(post("/api/v1/admin/restaurants/reject/10")
                        .param("comment", "")
                        .with(csrf())
                        .with(user(adminDetails)))
                .andExpect(status().isBadRequest());
    }
}
