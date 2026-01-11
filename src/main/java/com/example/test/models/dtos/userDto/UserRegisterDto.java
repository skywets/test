package com.example.test.models.dtos.userDto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterDto {

    private Long id;

    @NotBlank(message = "Name must not be empty")
    private String name;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email must not be empty")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    @NotBlank(message = "Password must not be empty")
    private String password;

}
