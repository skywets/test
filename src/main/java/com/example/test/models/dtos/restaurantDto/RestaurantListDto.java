package com.example.test.models.dtos.restaurantDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantListDto {

    private Long id;

    private String name;

    private String address;

    private boolean open;

    private Integer avgCookingTimeMinutes;

    private Double rating;

}