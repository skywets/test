package com.example.test.services.cartService.Impl;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.cartDto.MenuItemDto;
import com.example.test.models.entities.cart.MenuItem;
import com.example.test.models.entities.cuisine_foodType.FoodType;
import com.example.test.models.mappers.cartMapper.MenuItemMapper;
import com.example.test.repositories.cartRepo.MenuItemRepository;
import com.example.test.repositories.foodTypeRepo.FoodTypeRepository;
import com.example.test.services.cartService.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final FoodTypeRepository foodTypeRepository;
    private final MenuItemMapper menuItemMapper;

    @Override
    public MenuItemDto create(MenuItemDto dto) {
        MenuItem menuItem = menuItemMapper.toEntity(dto);

        if (dto.getFoodTypeIds() != null && !dto.getFoodTypeIds().isEmpty()) {
            List<FoodType> foodTypes = foodTypeRepository.findAllById(dto.getFoodTypeIds());
            menuItem.setFoodTypes(foodTypes);
        }

        MenuItem savedItem = menuItemRepository.save(menuItem);
        return menuItemMapper.toDto(savedItem);
    }

    @Override
    public MenuItemDto update(Long id, MenuItemDto dto) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("MenuItem with ID " + id + " not found"));

        menuItem.setName(dto.getName());
        menuItem.setPrice(dto.getPrice());
        menuItem.setAvailable(dto.isAvailable());
        menuItem.setQuantity(dto.getQuantity());
        menuItem.setCuisineType(dto.getCuisineType());

        if (dto.getFoodTypeIds() != null) {
            List<FoodType> foodTypes = foodTypeRepository.findAllById(dto.getFoodTypeIds());
            menuItem.setFoodTypes(foodTypes);
        }

        return menuItemMapper.toDto(menuItemRepository.save(menuItem));
    }

    @Override
    @Transactional(readOnly = true)
    public MenuItemDto getById(Long id) {
        return menuItemRepository.findById(id)
                .map(menuItemMapper::toDto)
                .orElseThrow(() -> new NotFoundException("MenuItem not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemDto> getAll() {
        return menuItemRepository.findAll().stream()
                .map(menuItemMapper::toDto)
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new NotFoundException("Cannot delete: MenuItem not found");
        }
        menuItemRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemDto> getAvailable() {
        return menuItemRepository.findByAvailableTrue().stream()
                .map(menuItemMapper::toDto)
                .toList();
    }
}
