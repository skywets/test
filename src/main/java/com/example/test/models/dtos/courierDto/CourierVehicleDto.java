package com.example.test.models.dtos.courierDto;

import com.example.test.models.entities.enums.VehicleType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourierVehicleDto {

    @NotNull(message = "VehicleType must not be null")
    private VehicleType vehicleType;
}
