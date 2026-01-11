package com.example.test.services.courierService;


import com.example.test.models.dtos.courierDto.CourierDto;
import com.example.test.models.entities.enums.VehicleType;

import java.util.List;

public interface CourierService {

    CourierDto updateVehicle(Long userId, VehicleType vehicleType);

    CourierDto updateStatus(Long userId, String status);

    CourierDto getCourier(Long id);

    List<CourierDto> getAllCouriers();

    void deleteCourier(Long id);


}