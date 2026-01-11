package com.example.test.controllers.admin;

import com.example.test.models.dtos.userDto.AssignRoleRequest;
import com.example.test.services.userService.AdminService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/assign-role")
    public ResponseEntity<?> assignRole(
            @Valid @RequestBody AssignRoleRequest request
    ) {
        adminService.assignRole(request.getUserId(), request.getRoleName());
        return ResponseEntity.ok("Role assigned");
    }
}

