package com.example.test.models.mappers.resMapper;

import com.example.test.models.dtos.restaurantDto.RestaurantApplicationDto;
import com.example.test.models.entities.restaurant.RestaurantApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RestaurantApplicationMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "status", target = "status")
    RestaurantApplicationDto toDto(RestaurantApplication app);

    RestaurantApplication toEntity(RestaurantApplicationDto dto);
}
