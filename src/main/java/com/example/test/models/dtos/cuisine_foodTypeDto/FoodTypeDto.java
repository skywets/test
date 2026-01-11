package com.example.test.models.dtos.cuisine_foodTypeDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FoodTypeDto {

    private Long id;

    @NotBlank
    private String name;
}
