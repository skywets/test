package com.example.test.services.resService;

import com.example.test.models.dtos.restaurantDto.RestaurantFilter;
import com.example.test.models.dtos.restaurantDto.RestaurantListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface Filter {

    Page<RestaurantListDto> findAll(
            RestaurantFilter filter,
            Pageable pageable
    );

}
