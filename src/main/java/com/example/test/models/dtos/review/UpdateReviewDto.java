package com.example.test.models.dtos.review;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateReviewDto {
    @NotBlank
    private String text;

    @NotNull
    @Min(1) @Max(5)
    private Double grade;
}
