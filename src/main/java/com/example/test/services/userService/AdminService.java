package com.example.test.services.userService;

import org.springframework.stereotype.Service;

@Service
public interface AdminService {
    void assignRole(Long userId, String roleName);

}
