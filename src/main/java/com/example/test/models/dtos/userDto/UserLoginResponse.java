package com.example.test.models.dtos.userDto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginResponse {

    @Email
    private String email;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}
