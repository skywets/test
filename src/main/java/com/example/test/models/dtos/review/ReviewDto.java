package com.example.test.models.dtos.review;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDto {

    private Long id;

    private String text;

    private Double grade;

    private Long orderId;

    private Long restaurantId;

    private LocalDateTime createdAt;
}
