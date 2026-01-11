package com.example.test.services.foodTypeService.impl;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.cuisine_foodTypeDto.FoodTypeDto;
import com.example.test.models.entities.cuisine_foodType.FoodType;
import com.example.test.models.mappers.cartMapper.FoodTypeMapper;
import com.example.test.repositories.foodTypeRepo.FoodTypeRepository;
import com.example.test.services.foodTypeService.FoodTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FoodTypeServiceImpl implements FoodTypeService {

    private final FoodTypeRepository foodTypeRepository;
    private final FoodTypeMapper foodTypeMapper;

    @Override
    public FoodTypeDto create(FoodTypeDto dto) {
        if (foodTypeRepository.existsByName(dto.getName())) {
            throw new RuntimeException("FoodType already exists");
        }

        FoodType foodType = foodTypeMapper.toEntity(dto);
        return foodTypeMapper.toDto(foodTypeRepository.save(foodType));
    }

    @Override
    public FoodTypeDto update(Long id, FoodTypeDto dto) {
        FoodType foodType = foodTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("FoodType not found"));

        foodType.setName(dto.getName());
        return foodTypeMapper.toDto(foodTypeRepository.save(foodType));
    }

    @Override
    @Transactional(readOnly = true)
    public FoodTypeDto getById(Long id) {
        return foodTypeMapper.toDto(
                foodTypeRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("FoodType not found"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodTypeDto> getAll() {
        return foodTypeMapper.toDtoList(foodTypeRepository.findAll());
    }

    @Override
    public void delete(Long id) {
        if (!foodTypeRepository.existsById(id)) {
            throw new NotFoundException("FoodType not found");
        }
        foodTypeRepository.deleteById(id);
    }
}
