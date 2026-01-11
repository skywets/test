package com.example.test.models.dtos.payment;

import com.example.test.models.entities.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDto {

    private Long id;

    private Long orderId;

    private PaymentStatus status;

    private BigDecimal amount;

    private LocalDateTime createdAt;
}
