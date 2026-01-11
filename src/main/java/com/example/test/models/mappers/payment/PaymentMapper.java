package com.example.test.models.mappers.payment;

import com.example.test.models.dtos.payment.CreatePaymentDto;
import com.example.test.models.dtos.payment.PaymentDto;
import com.example.test.models.entities.payment.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "order.id", target = "orderId")
    PaymentDto toDto(Payment payment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdAt", ignore = true)
    Payment toEntity(CreatePaymentDto dto);
}
