package com.example.test.controllers.restaurant;

import com.example.test.models.dtos.restaurantDto.RestaurantFilter;
import com.example.test.models.dtos.restaurantDto.RestaurantListDto;
import com.example.test.models.entities.cuisine.CuisineType;
import com.example.test.services.resService.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class FilterController {

    private final Filter restaurantService;

    @GetMapping("/filterByCuisineAndByRating")
    public Page<RestaurantListDto> getAll(
            @RequestParam(required = false) CuisineType cuisineType,
            @RequestParam(required = false) Double minRating,
            Pageable pageable
    ) {
        return restaurantService.findAll(
                new RestaurantFilter(cuisineType, minRating),
                pageable
        );
    }
}
