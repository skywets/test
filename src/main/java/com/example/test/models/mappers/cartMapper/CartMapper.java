package com.example.test.models.mappers.cartMapper;

import com.example.test.models.dtos.cartDto.CartDto;
import com.example.test.models.entities.cart.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CartItemMapper.class)
public interface CartMapper {

    @Mapping(source = "user.id", target = "userId")
    CartDto toDto(Cart entity);

    @Mapping(source = "userId", target = "user.id")
    Cart toEntity(CartDto dto);
}
