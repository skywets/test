package com.example.test.models.dtos.restaurantDto;

import lombok.Data;

@Data
public class RestaurantDto {

    private Long id;

    private Long ownerId;

    private String name;

    private String address;

    private boolean open;

    private Integer avgCookingTimeMinutes;

    private Double rating;
}