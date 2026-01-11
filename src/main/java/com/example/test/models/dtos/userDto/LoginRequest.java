package com.example.test.models.dtos.userDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email must not be empty")
    private String email;

    @NotBlank(message = "Password must not be empty")
    private String password;
}