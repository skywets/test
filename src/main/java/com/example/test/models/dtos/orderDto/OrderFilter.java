package com.example.test.models.dtos.orderDto;

import com.example.test.models.entities.enums.OrderStatus;

public record OrderFilter(
        Long userId,
        OrderStatus status
) {
}
