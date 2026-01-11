package com.example.test.models.dtos.courierDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourierStatusDto {

    @NotNull(message = "VehicleType must not be null")
    private String status; // WORKING, OFFLINE, BUSY,AVAILABLE
}
