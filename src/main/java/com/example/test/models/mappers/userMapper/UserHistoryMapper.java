package com.example.test.models.mappers.userMapper;

import com.example.test.models.dtos.userDto.UserHistoryDto;
import com.example.test.models.entities.user.User;
import com.example.test.models.entities.user.UserHistory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserHistoryMapper {

    UserHistory toEntity(User user);

    UserHistoryDto toDto(UserHistory history);
}
