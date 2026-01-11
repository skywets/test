package com.example.test.services.cartService;

import com.example.test.models.dtos.cartDto.MenuItemDto;

import java.util.List;

public interface MenuItemService {


    MenuItemDto create(MenuItemDto dto);

    MenuItemDto update(Long id, MenuItemDto dto);

    MenuItemDto getById(Long id);

    List<MenuItemDto> getAll();

    List<MenuItemDto> getAvailable();

    void delete(Long id);
}
