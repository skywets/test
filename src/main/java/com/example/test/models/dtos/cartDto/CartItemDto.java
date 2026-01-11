package com.example.test.models.dtos.cartDto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDto {

    private Long id;

    private MenuItemDto menuItemDto;

    private Integer quantity;
}
