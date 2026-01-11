package com.example.test.services.orderService;

import com.example.test.models.dtos.orderDto.OrderItemDto;

public interface OrderItemService {

    OrderItemDto getById(Long id);

    void delete(Long id);
}
