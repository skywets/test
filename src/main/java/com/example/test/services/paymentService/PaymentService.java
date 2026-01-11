package com.example.test.services.paymentService;

import com.example.test.models.dtos.payment.CreatePaymentDto;
import com.example.test.models.dtos.payment.PaymentDto;
import com.example.test.models.entities.enums.PaymentStatus;

public interface PaymentService {

    PaymentDto createPayment(CreatePaymentDto dto, Long currentUserId);

    PaymentDto getPaymentStatus(Long id, Long currentUserId);

    PaymentDto updatePaymentStatus(Long id, PaymentStatus newStatus);

    PaymentDto getByOrderId(Long orderId, Long currentUserId);

}
