package com.example.test.models.dtos.restaurantDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantProfileUpdateDto {

    @NotBlank(message = "Name must not be empty")
    private String name;

    @NotBlank(message = "Address must not be empty")
    private String address;

}
