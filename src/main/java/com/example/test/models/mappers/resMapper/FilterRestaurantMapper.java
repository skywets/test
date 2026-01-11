package com.example.test.models.mappers.resMapper;

import com.example.test.models.dtos.restaurantDto.RestaurantListDto;
import com.example.test.models.entities.restaurant.Restaurant;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FilterRestaurantMapper {

    RestaurantListDto toDto(Restaurant restaurant);

    Restaurant toEntity(RestaurantListDto dto);
}
