package com.example.test.models.dtos.orderDto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderRealtimeDto {

    private Long orderId;

    private String status; // OrderStatus

    private Double latitude;
    private Double longitude;

    private LocalDateTime updatedAt;
}
