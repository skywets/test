package com.example.test.models.mappers.userMapper;

import com.example.test.models.dtos.userDto.UserDto;
import com.example.test.models.dtos.userDto.UserRegisterDto;
import com.example.test.models.entities.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDTO(User model);

    User toModel(UserDto dto);

}
