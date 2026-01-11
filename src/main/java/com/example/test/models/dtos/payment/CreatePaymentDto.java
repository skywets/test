package com.example.test.models.dtos.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentDto {

    @NotNull
    private Long orderId;

    @NotNull
    @Positive
    private BigDecimal amount;
}



