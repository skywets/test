package com.example.test.models.dtos.resMenuDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMenuAvailabilityDto {

    @NotNull(message = "Availability status must be provided")
    private Boolean available;


}
