package com.example.test.models.dtos.orderDto;

import com.example.test.models.dtos.cartDto.MenuItemDto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDto {

    private Long id;

    private MenuItemDto menuItem;

    private int quantity;

    private BigDecimal price;
}
