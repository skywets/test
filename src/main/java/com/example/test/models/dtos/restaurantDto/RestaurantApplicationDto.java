package com.example.test.models.dtos.restaurantDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantApplicationDto {

    private Long id;

    private Long userId;

    private String documents;

    private String status;

    private String adminComment;
}