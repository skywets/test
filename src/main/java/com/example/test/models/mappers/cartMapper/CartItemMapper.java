package com.example.test.models.mappers.cartMapper;

import com.example.test.models.dtos.cartDto.CartItemDto;
import com.example.test.models.entities.cart.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = MenuItemMapper.class)
public interface CartItemMapper {

    @Mapping(source = "menuItem", target = "menuItemDto")
    CartItemDto toDto(CartItem entity);

    @Mapping(source = "menuItemDto", target = "menuItem")
    CartItem toEntity(CartItemDto dto);
}
