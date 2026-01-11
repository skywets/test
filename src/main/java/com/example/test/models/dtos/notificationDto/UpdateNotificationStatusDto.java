package com.example.test.models.dtos.notificationDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNotificationStatusDto {

    @NotBlank
    private String status;
}

