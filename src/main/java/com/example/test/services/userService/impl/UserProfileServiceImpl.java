package com.example.test.services.userService.impl;

import com.example.test.exceptions.InvalidCredentialsException;
import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.userDto.UpdatePasswordDto;
import com.example.test.models.dtos.userDto.UpdateProfileDto;
import com.example.test.models.dtos.userDto.UserDto;
import com.example.test.models.entities.user.User;
import com.example.test.models.entities.user.UserHistory;
import com.example.test.models.mappers.userMapper.UserHistoryMapper;
import com.example.test.models.mappers.userMapper.UserMapper;
import com.example.test.repositories.userRepo.UserHistoryRepository;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.userService.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserHistoryRepository historyRepository;
    private final UserHistoryMapper historyMapper;

    @Override
    public UserDto getCurrentProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toDTO(user);
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileDto dto) {
        User user = getUser(userId);

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        User savedUser = userRepository.save(user);
        UserHistory history = historyMapper.toEntity(savedUser);
        history.setUserId(user.getId());
        historyRepository.save(history);

    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordDto dto) {
        User user = getUser(userId);

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Old password incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        User savedUser = userRepository.save(user);
        UserHistory history = historyMapper.toEntity(savedUser);
        history.setUserId(user.getId());
        historyRepository.save(history);

    }


    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

}
