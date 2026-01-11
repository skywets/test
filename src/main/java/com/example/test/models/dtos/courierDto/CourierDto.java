package com.example.test.models.dtos.courierDto;

import com.example.test.models.entities.enums.CourierStatus;
import com.example.test.models.entities.enums.VehicleType;
import lombok.Data;

@Data
public class CourierDto {

    private Long id;

    private Long userId;

    private boolean available;

    private VehicleType vehicleType;

    private CourierStatus status;
}
