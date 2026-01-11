package com.example.test.services.resService;

import com.example.test.models.dtos.restaurantDto.RestaurantApplicationDto;

import java.util.List;

public interface RestaurantApplicationService {

    RestaurantApplicationDto createApplication(RestaurantApplicationDto dto);

    void approve(Long applicationId, Long adminId);

    void reject(Long applicationId, Long adminId, String comment);

    List<RestaurantApplicationDto> getByUser(Long userId);

}
