package com.example.test.services.courierService;

import com.example.test.models.dtos.orderDto.OrderDto;

import java.util.List;

public interface CourierAssignmentService {
    void assignOrderToCourier(Long courierId, Long orderId);

    List<OrderDto> getActiveOrders(Long courierId);
}
