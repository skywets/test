package com.example.test.models.dtos.userDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignRoleRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "roleName is required")
    private String roleName;
}
