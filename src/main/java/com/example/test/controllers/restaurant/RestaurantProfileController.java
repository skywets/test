package com.example.test.controllers.restaurant;

import com.example.test.models.dtos.restaurantDto.RestaurantDto;
import com.example.test.models.dtos.restaurantDto.RestaurantProfileUpdateDto;
import com.example.test.services.resService.RestaurantService;
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
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/restaurant/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RESTAURANT_OWNER')")
@Tag(name = "Restaurant Profile", description = "Endpoints for restaurant owners to manage their restaurants")
public class RestaurantProfileController {

    private final RestaurantService restaurantService;

    @Operation(summary = "Create a new restaurant", description = "Allows an owner to register a new restaurant profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Restaurant created successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: OWNER role required")
    })
    @PostMapping
    public ResponseEntity<RestaurantDto> create(
            @Valid @RequestBody RestaurantDto dto,
            @Parameter(hidden = true) Authentication auth
    ) {
        Long ownerId = getUserId(auth);
        RestaurantDto created = restaurantService.createRestaurant(ownerId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get my restaurants", description = "Returns a list of all restaurants owned by the current user.")
    @GetMapping
    public ResponseEntity<List<RestaurantDto>> myRestaurants(@Parameter(hidden = true) Authentication auth) {
        return ResponseEntity.ok(restaurantService.getMyRestaurants(getUserId(auth)));
    }

    @Operation(summary = "Update restaurant profile", description = "Updates detailed information of a specific restaurant.")
    @PutMapping("/{restaurantId}")
    public ResponseEntity<RestaurantDto> updateProfile(
            @Parameter(description = "ID of the restaurant") @PathVariable @NotNull @Positive Long restaurantId,
            @Valid @RequestBody RestaurantProfileUpdateDto dto,
            @Parameter(hidden = true) Authentication auth
    ) {
        return ResponseEntity.ok(restaurantService.updateProfile(
                restaurantId,
                getUserId(auth),
                dto
        ));
    }

    @Operation(summary = "Toggle restaurant open status", description = "Open or close the restaurant for orders.")
    @PatchMapping("/{restaurantId}/status")
    public ResponseEntity<RestaurantDto> updateStatus(
            @Parameter(description = "ID of the restaurant") @PathVariable @NotNull @Positive Long restaurantId,
            @Parameter(description = "Status: true to open, false to close") @RequestParam @NotNull boolean open,
            @Parameter(hidden = true) Authentication auth
    ) {
        return ResponseEntity.ok(restaurantService.updateOpenStatus(
                restaurantId,
                open,
                getUserId(auth)
        ));
    }

    private Long getUserId(Authentication auth) {
        return ((UserDetailsImpl) auth.getPrincipal()).getUser().getId();
    }
}
