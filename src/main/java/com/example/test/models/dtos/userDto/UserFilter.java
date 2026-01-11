package com.example.test.models.dtos.userDto;

import jakarta.validation.constraints.NotBlank;

public record UserFilter(
        @NotBlank(message = "Role cannot be blank")
        String role
) {
}
