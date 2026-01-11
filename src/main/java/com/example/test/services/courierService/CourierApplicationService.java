package com.example.test.services.courierService;

import com.example.test.models.dtos.courierDto.CourierApplicationDto;

import java.util.List;

public interface CourierApplicationService {
    CourierApplicationDto createApplication(CourierApplicationDto dto);

    List<CourierApplicationDto> getByUser(Long userId);

    void approve(Long applicationId, Long adminId);

    void reject(Long applicationId, Long adminId, String comment);

    CourierApplicationDto getById(Long id);

    List<CourierApplicationDto> getAll();

}