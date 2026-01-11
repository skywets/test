package com.example.test.models.dtos.restaurantDto;

public interface RestaurantWithRatingProjection {

    Long getId();

    String getName();

    String getAddress();

    boolean isOpen();
    Integer getAvgCookingTimeMinutes();
    Double getRating();
}
