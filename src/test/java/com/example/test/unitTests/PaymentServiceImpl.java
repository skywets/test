package com.example.test.unitTests;

import com.example.test.models.dtos.payment.CreatePaymentDto;
import com.example.test.models.dtos.payment.PaymentDto;
import com.example.test.models.entities.enums.OrderStatus;
import com.example.test.models.entities.enums.PaymentMethod;
import com.example.test.models.entities.enums.PaymentStatus;
import com.example.test.models.entities.order.Order;
import com.example.test.models.entities.payment.Payment;
import com.example.test.models.entities.user.User;
import com.example.test.models.mappers.payment.PaymentMapper;
import com.example.test.repositories.orderRepo.OrderRepository;
import com.example.test.repositories.paymentRepo.PaymentRepository;
import com.example.test.services.paymentService.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Service Unit Tests")
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User user1;
    private User user2;
    private Order order1;
    private Payment payment1;

    @BeforeEach
    void setUp() {
        user1 = User.builder().id(1L).email("user1@test.com").build();
        user2 = User.builder().id(2L).email("user2@test.com").build();

        order1 = new Order();
        order1.setId(10L);
        order1.setUser(user1);
        order1.setTotalPrice(new BigDecimal("50.00"));
        order1.setPaymentMethod(PaymentMethod.CARD);
        order1.setStatus(OrderStatus.CREATED);

        payment1 = new Payment();
        payment1.setId(100L);
        payment1.setOrder(order1);
        payment1.setAmount(new BigDecimal("50.00"));
        payment1.setStatus(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Create Payment: Success flow")
    void createPayment_Success() {
        CreatePaymentDto dto = CreatePaymentDto.builder()
                .orderId(10L)
                .amount(new BigDecimal("50.00"))
                .build();

        given(orderRepository.findById(10L)).willReturn(Optional.of(order1));
        given(paymentRepository.existsByOrderId(10L)).willReturn(false);
        given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(paymentMapper.toDto(any(Payment.class))).willReturn(new PaymentDto());


        paymentService.createPayment(dto, 1L);

        verify(paymentRepository).save(argThat(payment -> {
            assertThat(payment.getAmount()).isEqualByComparingTo("50.00");
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            return true;
        }));
    }

    @Test
    @DisplayName("Create Payment: Access Denied for foreign order")
    void createPayment_AccessDenied() {
        CreatePaymentDto dto = CreatePaymentDto.builder()
                .orderId(10L)
                .amount(new BigDecimal("50.00"))
                .build();

        given(orderRepository.findById(10L)).willReturn(Optional.of(order1));


        assertThatThrownBy(() -> paymentService.createPayment(dto, 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You can only pay for your own orders");
    }

    @Test
    @DisplayName("Create Payment: Throws exception if payment already exists")
    void createPayment_AlreadyExists() {
        CreatePaymentDto dto = CreatePaymentDto.builder()
                .orderId(10L)
                .amount(new BigDecimal("50.00"))
                .build();

        given(orderRepository.findById(10L)).willReturn(Optional.of(order1));
        given(paymentRepository.existsByOrderId(10L)).willReturn(true);


        assertThatThrownBy(() -> paymentService.createPayment(dto, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Payment for this order already exists");
    }

    @Test
    @DisplayName("Get Status: Success for owner")
    void getPaymentStatus_Success() {
        given(paymentRepository.findById(100L)).willReturn(Optional.of(payment1));
        given(paymentMapper.toDto(payment1)).willReturn(new PaymentDto());


        paymentService.getPaymentStatus(100L, 1L);


        verify(paymentRepository).findById(100L);
        verify(paymentMapper).toDto(payment1);
    }

    @Test
    @DisplayName("Get Status: Access Denied for stranger")
    void getPaymentStatus_AccessDenied() {
        given(paymentRepository.findById(100L)).willReturn(Optional.of(payment1));


        assertThatThrownBy(() -> paymentService.getPaymentStatus(100L, 2L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Update Status: Success Webhook (PAID) should confirm order")
    void updatePaymentStatus_Paid_ConfirmsOrder() {
        given(paymentRepository.findById(100L)).willReturn(Optional.of(payment1));
        given(paymentRepository.save(payment1)).willReturn(payment1);


        paymentService.updatePaymentStatus(100L, PaymentStatus.PAID);


        assertThat(payment1.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(order1.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(orderRepository).save(order1);
        verify(paymentRepository).save(payment1);
    }

    @Test
    @DisplayName("Update Status: Throws exception for CASH orders (must be manual)")
    void updatePaymentStatus_Cash_ThrowsException() {
        order1.setPaymentMethod(PaymentMethod.CASH);
        given(paymentRepository.findById(100L)).willReturn(Optional.of(payment1));


        assertThatThrownBy(() -> paymentService.updatePaymentStatus(100L, PaymentStatus.PAID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be updated for CASH orders");
    }
}

