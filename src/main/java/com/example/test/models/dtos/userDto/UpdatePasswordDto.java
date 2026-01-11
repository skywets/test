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
public class UpdatePasswordDto {

    @NotBlank(message = "OldPassword must not be empty")
    private String oldPassword;

    @NotBlank(message = "NewPassword must not be empty")
    private String newPassword;
}
