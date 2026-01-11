package com.example.test.models.dtos.courierDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourierApplicationDto {

    private Long id;

    private Long userId;

    @NotBlank(message = "Documents must not be empty")
    @Size(max = 1000, message = "Links/Info too long")
    private String documents;

    private String status;

    private String adminComment;

    private LocalDateTime createdAt;
}

