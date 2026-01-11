package com.example.test.models.dtos.userDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileDto {

    @NotBlank(message = "Name must not be empty")
    private String name;

    @NotBlank(message = "Email must not be empty")
    private String email;
}
