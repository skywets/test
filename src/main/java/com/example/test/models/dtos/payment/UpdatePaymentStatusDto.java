package com.example.test.models.dtos.payment;

import com.example.test.models.entities.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePaymentStatusDto {

    @NotNull
    private PaymentStatus status;
}
