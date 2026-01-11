package com.example.test.services.paymentService.impl;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.payment.CreatePaymentDto;
import com.example.test.models.dtos.payment.PaymentDto;
import com.example.test.models.entities.enums.OrderStatus;
import com.example.test.models.entities.enums.PaymentMethod;
import com.example.test.models.entities.enums.PaymentStatus;
import com.example.test.models.entities.order.Order;
import com.example.test.models.entities.payment.Payment;
import com.example.test.models.mappers.payment.PaymentMapper;
import com.example.test.repositories.orderRepo.OrderRepository;
import com.example.test.repositories.paymentRepo.PaymentRepository;
import com.example.test.services.paymentService.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentDto createPayment(CreatePaymentDto dto, Long currentUserId) {
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!order.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only pay for your own orders");
        }

        if (paymentRepository.existsByOrderId(dto.getOrderId())) {
            throw new IllegalStateException("Payment for this order already exists");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalPrice());
        payment.setStatus(PaymentStatus.PENDING);

        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDto getPaymentStatus(Long id, Long currentUserId) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        if (!payment.getOrder().getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Access denied");
        }

        return paymentMapper.toDto(payment);
    }

    @Override
    public PaymentDto updatePaymentStatus(Long id, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        if (payment.getOrder().getPaymentMethod() == PaymentMethod.CASH) {
            throw new IllegalStateException("Payment status cannot be updated for CASH orders.");
        }

        payment.setStatus(newStatus);

        if (newStatus == PaymentStatus.PAID) {
            Order order = payment.getOrder();
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }

        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDto getByOrderId(Long orderId, Long currentUserId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("No payment found for this order"));

        if (!payment.getOrder().getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Access denied");
        }

        return paymentMapper.toDto(payment);
    }

}
