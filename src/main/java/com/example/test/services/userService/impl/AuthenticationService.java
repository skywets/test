package com.example.test.services.userService.impl;

import com.example.test.exceptions.InvalidCredentialsException;
import com.example.test.exceptions.UserAlreadyExistsException;
import com.example.test.models.dtos.userDto.LoginRequest;
import com.example.test.models.dtos.userDto.UserRegisterDto;
import com.example.test.models.entities.user.RoleT;
import com.example.test.models.entities.user.User;
import com.example.test.models.entities.user.UserHistory;
import com.example.test.models.mappers.userMapper.UserHistoryMapper;
import com.example.test.repositories.userRepo.RoleRepository;
import com.example.test.repositories.userRepo.UserHistoryRepository;
import com.example.test.repositories.userRepo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserHistoryRepository historyRepository;
    private final UserHistoryMapper historyMapper;

    @Transactional
    public User signup(UserRegisterDto userRegisterDto) {

        if (userRepository.existsByEmail(userRegisterDto.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User with email %s already exists".formatted(userRegisterDto.getEmail())
            );
        }

        User user = new User();
        user.setName(userRegisterDto.getName());
        user.setEmail(userRegisterDto.getEmail());
        user.setPassword(passwordEncoder.encode(userRegisterDto.getPassword()));
        RoleT userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role USER missing"));

        user.getRoleTSet().add(userRole);

        User savedUser = userRepository.save(user);
        UserHistory history = historyMapper.toEntity(savedUser);
        history.setUserId(user.getId());
        historyRepository.save(history);

        return savedUser;
    }

    public User authenticate(LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password")
                );
    }
}