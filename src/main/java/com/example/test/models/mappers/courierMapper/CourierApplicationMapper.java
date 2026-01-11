package com.example.test.models.mappers.courierMapper;

import com.example.test.models.dtos.courierDto.CourierApplicationDto;
import com.example.test.models.entities.courier.CourierApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourierApplicationMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "status", target = "status")
    CourierApplicationDto toDto(CourierApplication model);
    CourierApplication toEntity(CourierApplicationDto dto);


}
