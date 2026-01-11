package com.example.test.services.foodTypeService;

import com.example.test.models.dtos.cuisine_foodTypeDto.FoodTypeDto;

import java.util.List;

public interface FoodTypeService {

    FoodTypeDto create(FoodTypeDto dto);

    FoodTypeDto update(Long id, FoodTypeDto dto);

    FoodTypeDto getById(Long id);

    List<FoodTypeDto> getAll();

    void delete(Long id);
}