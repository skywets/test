package com.example.test.models.mappers.courierMapper;

import com.example.test.models.dtos.courierDto.CourierDto;
import com.example.test.models.entities.courier.Courier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourierMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "status", target = "status")
    CourierDto toDto(Courier courier);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "status", target = "status")
    Courier toEntity(CourierDto dto);


}
