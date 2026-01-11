package com.example.test.controllers.restaurantMenuController;

import com.example.test.models.dtos.cartDto.MenuItemDto;
import com.example.test.models.dtos.resMenuDto.UpdateMenuAvailabilityDto;
import com.example.test.services.resMenuService.RestaurantMenuService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/menu")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RESTAURANT_OWNER')")
@Tag(name = "Restaurant Menu Management", description = "Endpoints for restaurant owners to manage their menu items")
public class RestaurantMenuController {

    private final RestaurantMenuService restaurantMenuService;

    private Long getOwnerId(Authentication auth) {
        return ((UserDetailsImpl) auth.getPrincipal()).getUser().getId();
    }

    @Operation(summary = "Add a new menu item", description = "Allows the restaurant owner to add a new item to the menu.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Menu item created successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Restaurant owner role required")
    })
    @PostMapping
    public ResponseEntity<MenuItemDto> addMenuItem(
            @Parameter(description = "ID of the restaurant") @PathVariable @NotNull @Positive Long restaurantId,
            @Valid @RequestBody MenuItemDto dto,
            @Parameter(hidden = true) Authentication auth
    ) {
        MenuItemDto createdItem = restaurantMenuService.addMenuItem(restaurantId, dto, getOwnerId(auth));
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @Operation(summary = "Get restaurant menu", description = "Publicly available endpoint to retrieve all menu items for a specific restaurant.")
    @GetMapping()
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<MenuItemDto>> getMenu(
            @Parameter(description = "ID of the restaurant") @PathVariable @NotNull @Positive Long restaurantId
    ) {
        return ResponseEntity.ok(restaurantMenuService.getMenu(restaurantId));
    }

    @Operation(summary = "Update menu item details", description = "Allows the owner to update name, price, or description of a menu item.")
    @PutMapping("/{menuItemId}")
    public ResponseEntity<MenuItemDto> updateMenuItem(
            @Parameter(description = "ID of the restaurant") @PathVariable @NotNull @Positive Long restaurantId,
            @Parameter(description = "ID of the menu item") @PathVariable @NotNull @Positive Long menuItemId,
            @Valid @RequestBody MenuItemDto dto,
            @Parameter(hidden = true) Authentication auth
    ) {
        MenuItemDto updatedItem = restaurantMenuService.updateMenuItem(restaurantId, menuItemId, dto, getOwnerId(auth));
        return ResponseEntity.ok(updatedItem);
    }

    @Operation(summary = "Remove menu item", description = "Permanently removes an item from the restaurant menu.")
    @DeleteMapping("/{menuItemId}")
    public ResponseEntity<Map<String, String>> removeMenuItem(
            @Parameter(description = "ID of the restaurant") @PathVariable @NotNull @Positive Long restaurantId,
            @Parameter(description = "ID of the menu item") @PathVariable @NotNull @Positive Long menuItemId,
            @Parameter(hidden = true) Authentication auth
    ) {
        restaurantMenuService.removeMenuItem(restaurantId, menuItemId, getOwnerId(auth));
        return ResponseEntity.ok(Map.of("message", "Menu item removed successfully"));
    }

    @Operation(summary = "Update menu item availability", description = "Toggles whether a menu item is currently available for order.")
    @PatchMapping("/{menuItemId}/availability")
    public ResponseEntity<MenuItemDto> updateAvailability(
            @Parameter(description = "ID of the restaurant") @PathVariable @NotNull @Positive Long restaurantId,
            @Parameter(description = "ID of the menu item") @PathVariable @NotNull @Positive Long menuItemId,
            @Valid @RequestBody UpdateMenuAvailabilityDto dto,
            @Parameter(hidden = true) Authentication auth
    ) {
        MenuItemDto updatedItem = restaurantMenuService.updateAvailability(restaurantId, menuItemId, dto.getAvailable(), getOwnerId(auth));
        return ResponseEntity.ok(updatedItem);
    }
}
