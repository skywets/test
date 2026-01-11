package com.example.test.controllers.reviewController;

import com.example.test.models.dtos.review.CreateReviewDto;
import com.example.test.models.dtos.review.ReviewDto;
import com.example.test.models.dtos.review.UpdateReviewDto;
import com.example.test.services.reviewService.ReviewService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Operations related to restaurant reviews and ratings")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Create a new review", description = "Allows an authenticated user to leave a review for a restaurant.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Review created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid review data")
    })
    @PostMapping
    public ResponseEntity<ReviewDto> create(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl principal,
            @Valid @RequestBody CreateReviewDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(principal.getUser().getId(), dto));
    }

    @Operation(summary = "Get reviews by restaurant", description = "Retrieves a list of reviews for a specific restaurant ID.")
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ReviewDto>> getByRestaurant(
            @Parameter(description = "Restaurant ID") @RequestParam @NotNull @Positive Long restaurantId
    ) {
        return ResponseEntity.ok(reviewService.getByRestaurant(restaurantId));
    }

    @Operation(summary = "Update an existing review", description = "Allows the owner or admin to update review content.")
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> update(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl principal,
            @Parameter(description = "Review ID") @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody UpdateReviewDto dto
    ) {
        Long userId = principal.getUser().getId();
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(reviewService.update(userId, isAdmin, id, dto));
    }

    @Operation(summary = "Delete a review", description = "Allows the owner or admin to delete a review.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl principal,
            @Parameter(description = "Review ID") @PathVariable @NotNull @Positive Long id
    ) {
        Long userId = principal.getUser().getId();
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        reviewService.delete(userId, isAdmin, id);
        return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
    }
}
