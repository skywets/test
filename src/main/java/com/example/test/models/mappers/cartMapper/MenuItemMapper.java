package com.example.test.models.mappers.cartMapper;

import com.example.test.models.dtos.cartDto.MenuItemDto;
import com.example.test.models.entities.cart.MenuItem;
import com.example.test.models.entities.cuisine_foodType.FoodType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring")
public interface MenuItemMapper {

    @Mapping(target = "foodTypes", ignore = true)
    MenuItem toEntity(MenuItemDto dto);

    @Mapping(
            target = "foodTypeIds",
            expression = "java(mapFoodTypeIds(entity.getFoodTypes()))"
    )
    MenuItemDto toDto(MenuItem entity);

    default List<Long> mapFoodTypeIds(List<FoodType> foodTypes) {
        if (foodTypes == null) return List.of();
        return foodTypes.stream()
                .map(FoodType::getId)
                .toList();
    }
}