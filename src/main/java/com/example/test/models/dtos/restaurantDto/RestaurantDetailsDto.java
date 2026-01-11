package com.example.test.models.dtos.restaurantDto;

import com.example.test.models.dtos.cartDto.MenuItemDto;
import lombok.Data;

import java.util.List;

@Data
public class RestaurantDetailsDto {

    private Long id;

    private String name;

    private String address;

    private boolean open;

    private Integer avgCookingTimeMinutes;

    private Double rating;

    private List<MenuItemDto> menu;
}
