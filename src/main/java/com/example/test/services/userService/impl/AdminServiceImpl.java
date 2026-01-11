package com.example.test.services.userService.impl;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.entities.user.RoleT;
import com.example.test.models.entities.user.User;
import com.example.test.repositories.userRepo.RoleRepository;
import com.example.test.repositories.userRepo.UserRepository;
import com.example.test.services.userService.AdminService;
import com.example.test.services.userService.UserHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final UserHistoryService userHistoryService;


    @Override
    public void assignRole(Long userId, String roleName) {

        User user = userRepo.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("User with id %s not found".formatted(userId))
                );

        RoleT role = roleRepo.findByName(roleName.toUpperCase())
                .orElseThrow(() ->
                        new NotFoundException("Role %s not found".formatted(roleName))
                );

        userHistoryService.saveHistory(user.getId());

        user.getRoleTSet().add(role);
        userRepo.save(user);
    }
}
