package com.example.test.services.courierService.impl;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.courierDto.CourierDto;
import com.example.test.models.entities.courier.Courier;
import com.example.test.models.entities.enums.CourierStatus;
import com.example.test.models.entities.enums.VehicleType;
import com.example.test.models.mappers.courierMapper.CourierMapper;
import com.example.test.repositories.courierRepo.CourierRepository;
import com.example.test.services.courierService.CourierService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CourierServiceImpl implements CourierService {

    private final CourierRepository courierRepository;
    private final CourierMapper courierMapper;

    @Override
    @Transactional
    public CourierDto updateVehicle(Long userId, VehicleType vehicleType) {

        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null");
        }

        Courier courier = courierRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Courier profile for user ID " + userId + " not found"));

        courier.setVehicleType(vehicleType);

        return courierMapper.toDto(courierRepository.save(courier));
    }

    @Override
    @Transactional
    public CourierDto updateStatus(Long userId, String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status string cannot be empty");
        }

        Courier courier = courierRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Courier profile for user ID " + userId + " not found"));

        CourierStatus newStatus;
        try {
            newStatus = CourierStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }

        if (newStatus != CourierStatus.OFFLINE && newStatus != CourierStatus.AVAILABLE) {
            throw new AccessDeniedException(
                    "Courier manually can only switch to OFFLINE or AVAILABLE status"
            );
        }

        courier.setStatus(newStatus);

        courier.setAvailable(newStatus == CourierStatus.AVAILABLE);

        return courierMapper.toDto(courierRepository.save(courier));
    }

    @Override
    @Transactional(readOnly = true)
    public CourierDto getCourier(Long id) {
        return courierRepository.findById(id)
                .map(courierMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Courier not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourierDto> getAllCouriers() {
        return courierRepository.findAll()
                .stream()
                .map(courierMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteCourier(Long id) {
        if (!courierRepository.existsById(id)) {
            throw new NotFoundException("Cannot delete: Courier not found with ID: " + id);
        }
        courierRepository.deleteById(id);
    }
}