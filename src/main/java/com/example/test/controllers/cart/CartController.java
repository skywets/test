package com.example.test.controllers.cart;

import com.example.test.models.dtos.cartDto.CartDto;
import com.example.test.services.cartService.CartService;
import com.example.test.services.userService.impl.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Validated
@Tag(name = "Cart", description = "Operations for managing items in the user's shopping cart")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get current user's cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CartDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cart not found", content = @Content)
    })
    @GetMapping
    public ResponseEntity<CartDto> getCart(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(cartService.getCurrentCart(userDetails.getUser().getId()));
    }

    @Operation(summary = "Add item to cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item added successfully",
                    content = @Content(schema = @Schema(implementation = CartDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters (negative quantity, etc.)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Menu item not found", content = @Content)
    })
    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(
            @RequestParam @NotNull @Positive Long menuItemId,
            @RequestParam @NotNull @Min(1) Integer quantity,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addItem(userDetails.getUser().getId(), menuItemId, quantity));
    }

    @Operation(summary = "Update item quantity in cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quantity updated successfully",
                    content = @Content(schema = @Schema(implementation = CartDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - Not your cart", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cart item not found", content = @Content)
    })
    @PutMapping("/items/{id}")
    public ResponseEntity<CartDto> updateItem(
            @PathVariable @NotNull @Positive Long id,
            @RequestParam @NotNull @Min(1) Integer quantity,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(cartService.updateItem(userDetails.getUser().getId(), id, quantity));
    }

    @Operation(summary = "Remove item from cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item removed successfully",
                    content = @Content(schema = @Schema(example = "{\"message\": \"Item removed from cart\"}"))),
            @ApiResponse(responseCode = "404", description = "Cart item not found", content = @Content)
    })
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Map<String, String>> removeItem(
            @PathVariable @NotNull @Positive Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        cartService.removeItem(userDetails.getUser().getId(), id);
        return ResponseEntity.ok(Map.of("message", "Item removed from cart"));
    }

    @Operation(summary = "Clear the entire cart")
    @ApiResponse(responseCode = "200", description = "Cart cleared successfully")
    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearCart(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        cartService.clearCart(userDetails.getUser().getId());
        return ResponseEntity.ok(Map.of("message", "Cart cleared successfully"));
    }
}
