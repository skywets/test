package com.example.test.controllers;

import com.example.test.controllers.restaurant.RestaurantApplicationController;
import com.example.test.exceptions.GlobalExceptionHandler;
import com.example.test.models.dtos.restaurantDto.RestaurantApplicationDto;
import com.example.test.models.entities.user.RoleT;
import com.example.test.models.entities.user.User;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.security.JwtAuthenticationFilter;
import com.example.test.services.resService.RestaurantApplicationService;
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
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestaurantApplicationController.class)
@Import(GlobalExceptionHandler.class)
@EnableMethodSecurity
class RestaurantApplicationControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestaurantApplicationService applicationService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    private UserDetailsImpl userDetails;
    private UsernamePasswordAuthenticationToken userAuth;
    private User testUser;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        RoleT userRole = RoleT.builder().name("USER").permissionTSet(new HashSet<>()).build();
        testUser = User.builder()
                .id(1L)
                .email("user@test.com")
                .roleTSet(Set.of(userRole))
                .active(true)
                .build();

        userDetails = new UserDetailsImpl(testUser);
        userAuth = new UsernamePasswordAuthenticationToken(userDetails, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .alwaysDo(print())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/applications/restaurants - Success submission")
    void create_Success() throws Exception {
        RestaurantApplicationDto dto = new RestaurantApplicationDto();
        given(userRepository.findByEmail("user@test.com")).willReturn(Optional.of(testUser));
        given(applicationService.createApplication(any())).willReturn(dto);

        mockMvc.perform(post("/api/v1/applications/restaurants")
                        .with(csrf())
                        .with(user(userDetails))
                        .principal(userAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/applications/restaurants - Success retrieval of user applications")
    void myApplications_Success() throws Exception {
        given(applicationService.getByUser(1L)).willReturn(List.of(new RestaurantApplicationDto()));

        mockMvc.perform(get("/api/v1/applications/restaurants")
                        .with(user(userDetails))
                        .principal(userAuth))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/applications/restaurants - Unauthorized for Anonymous Guest")
    void create_UnauthorizedForGuest() throws Exception {
        mockMvc.perform(post("/api/v1/applications/restaurants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

}
