package com.example.test.controllers.restaurant;

import com.example.test.models.dtos.restaurantDto.RestaurantDetailsDto;
import com.example.test.models.dtos.restaurantDto.RestaurantDto;
import com.example.test.services.resService.RestaurantApplicationService;
import com.example.test.services.resService.RestaurantService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/admin/restaurants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Restaurant Management", description = "Administrative operations for managing restaurant applications and records")
public class AdminRestaurantController {

    private final RestaurantApplicationService restaurantApplicationService;
    private final RestaurantService restaurantService;

    @Operation(summary = "Approve restaurant application", description = "Approves a pending restaurant application by its ID. [ADMIN ONLY]")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application approved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @PostMapping("/approve/{id}")
    public ResponseEntity<Map<String, String>> approve(
            @Parameter(description = "Application ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(hidden = true) Authentication auth
    ) {
        restaurantApplicationService.approve(id, getAdminId(auth));
        return ResponseEntity.ok(Map.of("message", "Restaurant application approved successfully"));
    }

    @Operation(summary = "Reject restaurant application", description = "Rejects a pending application with a mandatory comment. [ADMIN ONLY]")
    @PostMapping("/reject/{id}")
    public ResponseEntity<Map<String, String>> reject(
            @Parameter(description = "Application ID") @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "Reason for rejection") @RequestParam @NotBlank(message = "Rejection comment is required") String comment,
            @Parameter(hidden = true) Authentication auth
    ) {
        restaurantApplicationService.reject(id, getAdminId(auth), comment);
        return ResponseEntity.ok(Map.of("message", "Restaurant application rejected successfully"));
    }

    @Operation(summary = "Get restaurant details", description = "Retrieves full details of a specific restaurant.")
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDetailsDto> getById(
            @Parameter(description = "Restaurant ID") @PathVariable @NotNull @Positive Long id
    ) {
        return ResponseEntity.ok(restaurantService.getById(id));
    }

    @Operation(summary = "Get all restaurants", description = "Returns a list of all restaurants in the system.")
    @GetMapping
    public ResponseEntity<List<RestaurantDto>> getAll() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    @Operation(summary = "Delete restaurant", description = "Permanently deletes a restaurant record from the system. [ADMIN ONLY]")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            @Parameter(description = "Restaurant ID") @PathVariable @NotNull @Positive Long id
    ) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok(Map.of("message", "Restaurant deleted successfully by admin"));
    }

    private Long getAdminId(Authentication auth) {
        return ((UserDetailsImpl) auth.getPrincipal()).getUser().getId();
    }
}
