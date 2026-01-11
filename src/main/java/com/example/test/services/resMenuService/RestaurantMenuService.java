package com.example.test.services.resMenuService;

import com.example.test.models.dtos.cartDto.MenuItemDto;

import java.util.List;

public interface RestaurantMenuService {

    MenuItemDto addMenuItem(Long restaurantId, MenuItemDto dto, Long ownerId);

    MenuItemDto updateMenuItem(
            Long restaurantId,
            Long menuItemId,
            MenuItemDto dto,
            Long ownerId
    );

    void removeMenuItem(Long restaurantId, Long menuItemId, Long ownerId);

    MenuItemDto updateAvailability(
            Long restaurantId,
            Long menuItemId,
            boolean available,
            Long ownerId
    );

    List<MenuItemDto> getMenu(Long restaurantId);
}
