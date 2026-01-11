package com.example.test.models.dtos.restaurantDto;

import com.example.test.models.entities.cuisine.CuisineType;

public record RestaurantFilter(
        CuisineType cuisineType,
        Double minRating
) {}
