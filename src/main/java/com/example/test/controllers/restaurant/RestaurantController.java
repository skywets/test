package com.example.test.controllers.restaurant;

import com.example.test.models.dtos.restaurantDto.RestaurantDetailsDto;
import com.example.test.models.dtos.restaurantDto.RestaurantDto;
import com.example.test.services.resService.RestaurantService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/restaurant")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDetailsDto> getById(@PathVariable @NotNull @Positive Long id) {
        return ResponseEntity.ok(restaurantService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<RestaurantDto>> getAll() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable @NotNull @Positive Long id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok(Map.of("message", "Restaurant deleted successfully"));
    }
}
