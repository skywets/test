package com.example.test.models.dtos.orderDto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderTrackingDto {

    private Long orderId;

    private Double latitude;

    private Double longitude;

    private String status;

    private LocalDateTime updatedAt;
}
