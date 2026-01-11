package com.example.test.models.dtos.cartDto;

import com.example.test.models.entities.cuisine.CuisineType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDto {

    private Long id;

    @NotBlank
    private String name;

    @NotNull
    @Positive
    private BigDecimal price;

    private boolean available;

    @NotNull
    @PositiveOrZero
    private Integer quantity;

    @NotNull
    private CuisineType cuisineType;

    private List<Long> foodTypeIds;
}

