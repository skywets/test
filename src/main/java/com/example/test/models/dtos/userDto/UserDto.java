package com.example.test.models.dtos.userDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDto {

    @NotBlank(message = "Name must not be empty")
    private String name;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email must not be empty")
    private String email;
}
