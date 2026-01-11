package com.example.test.controllers.cart;

import com.example.test.models.dtos.cartDto.MenuItemDto;
import com.example.test.services.cartService.MenuItemService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/menu-items")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
@Tag(name = "Menu Items", description = "Operations for managing menu items (Administration and Public view)")
public class MenuItemController {

    private final MenuItemService menuItemService;

    @Operation(summary = "Create a new menu item [ADMIN]")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item created successfully",
                    content = @Content(schema = @Schema(implementation = MenuItemDto.class))),
            @ApiResponse(responseCode = "400", description = "DTO validation error", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required", content = @Content)
    })
    @PostMapping
    public ResponseEntity<MenuItemDto> create(@RequestBody @Valid MenuItemDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuItemService.create(dto));
    }

    @Operation(summary = "Update an existing menu item [ADMIN]")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item updated successfully"),
            @ApiResponse(responseCode = "404", description = "Item not found", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<MenuItemDto> update(
            @PathVariable @NotNull @Positive Long id,
            @RequestBody @Valid MenuItemDto dto
    ) {
        return ResponseEntity.ok(menuItemService.update(id, dto));
    }

    @Operation(summary = "Get menu item by ID [PUBLIC]")
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved item",
                    content = @Content(schema = @Schema(implementation = MenuItemDto.class))),
            @ApiResponse(responseCode = "404", description = "Item not found", content = @Content)
    })
    public ResponseEntity<MenuItemDto> getById(@PathVariable @NotNull @Positive Long id) {
        return ResponseEntity.ok(menuItemService.getById(id));
    }

    @Operation(summary = "Get all menu items [PUBLIC]")
    @GetMapping
    @PreAuthorize("permitAll()")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all items")
    public ResponseEntity<List<MenuItemDto>> getAll() {
        return ResponseEntity.ok(menuItemService.getAll());
    }

    @Operation(summary = "Delete menu item [ADMIN]")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Item not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable @NotNull @Positive Long id) {
        menuItemService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Menu item with ID %d has been deleted successfully".formatted(id)));
    }
}
