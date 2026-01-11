package com.example.test.controllers.foodTypeController;

import com.example.test.models.dtos.cuisine_foodTypeDto.FoodTypeDto;
import com.example.test.services.foodTypeService.FoodTypeService;
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

@Validated
@RestController
@RequestMapping("/api/v1/food-types")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Food Types", description = "Management of food categories")
public class FoodTypeController {

    private final FoodTypeService foodTypeService;

    @Operation(summary = "Create a new food type [ADMIN]")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Food type created",
                    content = @Content(schema = @Schema(implementation = FoodTypeDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required")
    })
    @PostMapping
    public ResponseEntity<FoodTypeDto> create(@RequestBody @Valid FoodTypeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(foodTypeService.create(dto));
    }

    @Operation(summary = "Update an existing food type [ADMIN]")
    @PutMapping("/{id}")
    public ResponseEntity<FoodTypeDto> update(
            @PathVariable @NotNull @Positive Long id,
            @RequestBody @Valid FoodTypeDto dto
    ) {
        return ResponseEntity.ok(foodTypeService.update(id, dto));
    }

    @Operation(summary = "Get food type by ID [PUBLIC]")
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Food type not found")
    })
    public ResponseEntity<FoodTypeDto> getById(@PathVariable @NotNull @Positive Long id) {
        return ResponseEntity.ok(foodTypeService.getById(id));
    }

    @Operation(summary = "Get all food types [PUBLIC]")
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<FoodTypeDto>> getAll() {
        return ResponseEntity.ok(foodTypeService.getAll());
    }

    @Operation(summary = "Delete a food type [ADMIN]")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable @NotNull @Positive Long id) {
        foodTypeService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Food type deleted successfully"));
    }
}
