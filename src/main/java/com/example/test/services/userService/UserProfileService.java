package com.example.test.services.userService;

import com.example.test.models.dtos.userDto.UpdatePasswordDto;
import com.example.test.models.dtos.userDto.UpdateProfileDto;
import com.example.test.models.dtos.userDto.UserDto;
import org.springframework.stereotype.Service;

@Service
public interface UserProfileService {

    UserDto getCurrentProfile(String email);

    void updateProfile(Long userId, UpdateProfileDto dto);

    void updatePassword(Long userId, UpdatePasswordDto dto);
}
