package com.example.test.services.resMenuService.impl;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.cartDto.MenuItemDto;
import com.example.test.models.entities.cart.MenuItem;
import com.example.test.models.entities.restaurant.Restaurant;
import com.example.test.models.mappers.cartMapper.MenuItemMapper;
import com.example.test.repositories.cartRepo.MenuItemRepository;
import com.example.test.repositories.foodTypeRepo.FoodTypeRepository;
import com.example.test.repositories.resRepo.FilterRestaurantRepository;
import com.example.test.services.resMenuService.RestaurantMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantMenuServiceImpl implements RestaurantMenuService {

    private final FilterRestaurantRepository restaurantRepo;
    private final MenuItemRepository menuItemRepo;
    private final FoodTypeRepository foodTypeRepository;
    private final MenuItemMapper mapper;


    private Restaurant getOwnedRestaurant(Long restaurantId, Long ownerId) {
        Restaurant restaurant = restaurantRepo.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));

        if (!restaurant.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("You are not owner of this restaurant");
        }

        return restaurant;
    }

    @Override
    public MenuItemDto addMenuItem(Long restaurantId, MenuItemDto dto, Long ownerId) {

        Restaurant restaurant = getOwnedRestaurant(restaurantId, ownerId);

        MenuItem menuItem = mapper.toEntity(dto);

        if (dto.getFoodTypeIds() != null && !dto.getFoodTypeIds().isEmpty()) {
            menuItem.setFoodTypes(
                    foodTypeRepository.findAllById(dto.getFoodTypeIds())
            );
        }

        menuItemRepo.save(menuItem);
        restaurant.getMenuItems().add(menuItem);

        return mapper.toDto(menuItem);
    }


    @Override
    public MenuItemDto updateMenuItem(
            Long restaurantId,
            Long menuItemId,
            MenuItemDto dto,
            Long ownerId
    ) {

        Restaurant restaurant = getOwnedRestaurant(restaurantId, ownerId);

        MenuItem menuItem = menuItemRepo.findById(menuItemId)
                .orElseThrow(() -> new NotFoundException("Menu item not found"));

        if (!restaurant.getMenuItems().contains(menuItem)) {
            throw new AccessDeniedException("Menu item does not belong to this restaurant");
        }

        menuItem.setName(dto.getName());
        menuItem.setPrice(dto.getPrice());
        menuItem.setQuantity(dto.getQuantity());
        menuItem.setAvailable(dto.isAvailable());
        menuItem.setCuisineType(dto.getCuisineType());

        if (dto.getFoodTypeIds() != null) {
            menuItem.setFoodTypes(
                    foodTypeRepository.findAllById(dto.getFoodTypeIds())
            );
        }

        return mapper.toDto(menuItemRepo.save(menuItem));
    }


    @Override
    public void removeMenuItem(Long restaurantId, Long menuItemId, Long ownerId) {

        Restaurant restaurant = getOwnedRestaurant(restaurantId, ownerId);

        boolean removed = restaurant.getMenuItems()
                .removeIf(mi -> mi.getId().equals(menuItemId));

        if (!removed) {
            throw new NotFoundException("Menu item not found in this restaurant");
        }
    }


    @Override
    public MenuItemDto updateAvailability(
            Long restaurantId,
            Long menuItemId,
            boolean available,
            Long ownerId
    ) {

        Restaurant restaurant = getOwnedRestaurant(restaurantId, ownerId);

        MenuItem menuItem = menuItemRepo.findById(menuItemId)
                .orElseThrow(() -> new NotFoundException("Menu item not found"));

        if (!restaurant.getMenuItems().contains(menuItem)) {
            throw new AccessDeniedException("Menu item does not belong to this restaurant");
        }

        menuItem.setAvailable(available);
        return mapper.toDto(menuItemRepo.save(menuItem));
    }


    @Override
    @Transactional(readOnly = true)
    public List<MenuItemDto> getMenu(Long restaurantId) {

        Restaurant restaurant = restaurantRepo.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));

        return restaurant.getMenuItems()
                .stream()
                .map(mapper::toDto)
                .toList();
    }
}
