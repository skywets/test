package com.example.test.services.cartService;

import com.example.test.models.dtos.cartDto.CartDto;

public interface CartService {

    CartDto getCurrentCart(Long userId);

    CartDto addItem(Long userId, Long menuItemId, Integer quantity);

    CartDto updateItem(Long userId, Long cartItemId, Integer quantity);

    void removeItem(Long userId, Long cartItemId);

    void clearCart(Long userId);
}