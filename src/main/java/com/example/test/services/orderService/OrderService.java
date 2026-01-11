package com.example.test.services.orderService;

import com.example.test.models.dtos.orderDto.OrderDto;
import com.example.test.models.dtos.orderDto.OrderFilter;
import com.example.test.models.entities.enums.OrderStatus;
import com.example.test.models.entities.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface OrderService {

    OrderDto createOrderFromCart(Long userId, Long restaurantId, PaymentMethod paymentMethod);

    OrderDto getById(Long id);

    List<OrderDto> getUserOrders(Long userId);

    Page<OrderDto> findAllByFilter(OrderFilter filter, Pageable pageable, Authentication auth);

    OrderDto updateStatus(Authentication authentication, Long orderId, OrderStatus status);

    void cancel(Long orderId, Authentication auth);
}