package com.example.test.controllers;

import com.example.test.controllers.reviewController.ReviewController;
import com.example.test.exceptions.GlobalExceptionHandler;
import com.example.test.models.dtos.review.CreateReviewDto;
import com.example.test.models.dtos.review.ReviewDto;
import com.example.test.models.dtos.review.UpdateReviewDto;
import com.example.test.models.entities.user.RoleT;
import com.example.test.models.entities.user.User;
import com.example.test.security.JwtAuthenticationFilter;
import com.example.test.services.reviewService.ReviewService;
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

@WebMvcTest(ReviewController.class)
@Import(GlobalExceptionHandler.class)
@EnableMethodSecurity
class ReviewControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

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
    @DisplayName("POST /api/v1/reviews - Success creation with valid DTO")
    void createReview_Success() throws Exception {
        CreateReviewDto dto = CreateReviewDto.builder()
                .orderId(500L)
                .text("Excellent delivery and hot pizza!")
                .grade(5.0)
                .build();

        given(reviewService.createReview(eq(1L), any(CreateReviewDto.class))).willReturn(new ReviewDto());

        mockMvc.perform(post("/api/v1/reviews")
                        .with(csrf())
                        .with(user(userDetails))
                        .principal(userAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/reviews - Validation fail (missing orderId and invalid grade)")
    void createReview_ValidationFail() throws Exception {
        CreateReviewDto invalidDto = CreateReviewDto.builder()
                .orderId(null)
                .text("")
                .grade(6.0)
                .build();

        mockMvc.perform(post("/api/v1/reviews")
                        .with(csrf())
                        .with(user(userDetails))
                        .principal(userAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.orderId").value("must not be null"))
                .andExpect(jsonPath("$.fields.text").value("must not be blank"))
                .andExpect(jsonPath("$.fields.grade").exists());
    }

    @Test
    @DisplayName("PUT /api/v1/reviews/{id} - Success update by owner")
    void updateReview_Success() throws Exception {
        UpdateReviewDto updateDto = UpdateReviewDto.builder()
                .text("Update")
                .grade(4.6)
                .build();

        given(reviewService.update(eq(1L), eq(false), eq(100L), any())).willReturn(new ReviewDto());

        mockMvc.perform(put("/api/v1/reviews/100")
                        .with(csrf())
                        .with(user(userDetails))
                        .principal(userAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/reviews - Retrieval success")
    void getByRestaurant_Success() throws Exception {
        given(reviewService.getByRestaurant(10L)).willReturn(List.of(new ReviewDto()));

        mockMvc.perform(get("/api/v1/reviews")
                        .param("restaurantId", "10")
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }
}
