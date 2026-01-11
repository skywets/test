package com.example.test.services.resService;


import com.example.test.models.dtos.restaurantDto.RestaurantDetailsDto;
import com.example.test.models.dtos.restaurantDto.RestaurantDto;
import com.example.test.models.dtos.restaurantDto.RestaurantProfileUpdateDto;

import java.util.List;

public interface RestaurantService {

    RestaurantDto createRestaurant(Long ownerId, RestaurantDto dto);

    RestaurantDetailsDto getById(Long restaurantId);

    List<RestaurantDto> getAllRestaurants();

    void deleteRestaurant(Long id);


    List<RestaurantDto> getMyRestaurants(Long ownerId);


    RestaurantDto updateProfile(
            Long restaurantId,
            Long ownerId,
            RestaurantProfileUpdateDto dto
    );


    RestaurantDto updateOpenStatus(
            Long restaurantId,
            boolean open,
            Long ownerId
    );

}
