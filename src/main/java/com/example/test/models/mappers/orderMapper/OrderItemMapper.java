package com.example.test.models.mappers.orderMapper;

import com.example.test.models.dtos.orderDto.OrderItemDto;
import com.example.test.models.entities.order.OrderItem;
import com.example.test.models.mappers.cartMapper.MenuItemMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = MenuItemMapper.class
)
public interface OrderItemMapper {

    OrderItemDto toDto(OrderItem entity);

    OrderItem toEntity(OrderItemDto dto);

    List<OrderItemDto> toDtoList(List<OrderItem> entities);

    List<OrderItem> toEntityList(List<OrderItemDto> dtos);
}
