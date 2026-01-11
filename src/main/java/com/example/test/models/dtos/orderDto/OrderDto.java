package com.example.test.models.dtos.orderDto;

import com.example.test.models.entities.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDto {

    private Long id;

    private Long userId;

    private Long courierId;

    private Long restaurantId;

    private OrderStatus status;

    private BigDecimal totalPrice;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
