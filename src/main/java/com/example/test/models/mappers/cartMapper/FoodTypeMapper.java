package com.example.test.models.mappers.cartMapper;

import com.example.test.models.dtos.cuisine_foodTypeDto.FoodTypeDto;
import com.example.test.models.entities.cuisine_foodType.FoodType;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FoodTypeMapper {

    FoodType toEntity(FoodTypeDto dto);

    FoodTypeDto toDto(FoodType entity);

    List<FoodTypeDto> toDtoList(List<FoodType> entities);
}
