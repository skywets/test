package com.example.test.models.mappers.orderMapper;

import com.example.test.models.dtos.orderDto.OrderDto;
import com.example.test.models.entities.order.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "courier.id", target = "courierId")
    @Mapping(source = "restaurant.id", target = "restaurantId")
    @Mapping(source = "status", target = "status")
    OrderDto toDto(Order order);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "courier", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(source = "status", target = "status")
    Order toEntity(OrderDto dto);
}