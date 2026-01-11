package com.example.test.services.userService;

import com.example.test.models.dtos.userDto.UserDto;
import com.example.test.models.dtos.userDto.UserFilter;
import com.example.test.models.dtos.userDto.UserRegisterDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    void create(UserRegisterDto userRegisterDto);

    UserDto findById(Long id);

    Page<UserDto> findAllByFilter(UserFilter filter, Pageable pageable);

    void deactivate(Long userId);


}