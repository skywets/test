package com.example.test.models.mappers.resMapper;

import com.example.test.models.dtos.restaurantDto.RestaurantDto;
import com.example.test.models.entities.restaurant.Restaurant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(target = "rating", ignore = true)
    RestaurantDto toDto(Restaurant restaurant);

    @Mapping(source = "ownerId", target = "owner.id")
    Restaurant toEntity(RestaurantDto dto);
}
