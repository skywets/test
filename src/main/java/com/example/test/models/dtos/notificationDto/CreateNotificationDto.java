package com.example.test.models.dtos.notificationDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateNotificationDto {

    @NotNull
    private Long userId;

    @NotBlank
    @Size(max = 500)
    private String message;

    private LocalDateTime sendAt;
}
