package com.example.test.controllers;

import com.example.test.controllers.restaurant.RestaurantProfileController;
import com.example.test.exceptions.GlobalExceptionHandler;
import com.example.test.models.dtos.restaurantDto.RestaurantDto;
import com.example.test.models.dtos.restaurantDto.RestaurantProfileUpdateDto;
import com.example.test.models.entities.user.RoleT;
import com.example.test.models.entities.user.User;
import com.example.test.security.JwtAuthenticationFilter;
import com.example.test.services.resService.RestaurantService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestaurantProfileController.class)
@Import(GlobalExceptionHandler.class)
@EnableMethodSecurity
class RestaurantProfileControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestaurantService restaurantService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    private UserDetailsImpl ownerDetails;
    private UsernamePasswordAuthenticationToken ownerAuth;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        RoleT ownerRole = RoleT.builder().name("RESTAURANT_OWNER").permissionTSet(new HashSet<>()).build();
        User ownerUser = User.builder().id(10L).email("owner@test.com").roleTSet(Set.of(ownerRole)).active(true).build();
        ownerDetails = new UserDetailsImpl(ownerUser);
        ownerAuth = new UsernamePasswordAuthenticationToken(ownerDetails, null, List.of(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER")));

        RoleT userRole = RoleT.builder().name("USER").permissionTSet(new HashSet<>()).build();
        User normalUser = User.builder().id(1L).email("user@test.com").roleTSet(Set.of(userRole)).active(true).build();
        userDetails = new UserDetailsImpl(normalUser);

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .alwaysDo(print())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/restaurant/profile - Success creation by owner")
    void create_Success() throws Exception {
        RestaurantDto dto = new RestaurantDto();
        dto.setName("New Pizza");

        given(restaurantService.createRestaurant(eq(10L), any())).willReturn(dto);

        mockMvc.perform(post("/api/v1/restaurant/profile")
                        .with(csrf())
                        .with(user(ownerDetails))
                        .principal(ownerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /api/v1/restaurant/profile - Forbidden for normal USER")
    void myRestaurants_ForbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/v1/restaurant/profile")
                        .with(user(userDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/restaurant/profile/{id} - Success update")
    void updateProfile_Success() throws Exception {
        RestaurantProfileUpdateDto dto = RestaurantProfileUpdateDto.builder()
                .name("New Restaurant")
                .address("New York")
                .build();

        mockMvc.perform(put("/api/v1/restaurant/profile/50")
                        .with(csrf())
                        .with(user(ownerDetails))
                        .principal(ownerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /status - Success toggling open status")
    void updateStatus_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/restaurant/profile/50/status")
                        .param("open", "true")
                        .with(csrf())
                        .with(user(ownerDetails))
                        .principal(ownerAuth))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/restaurant/profile - Unauthorized for Anonymous")
    void create_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/restaurant/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
