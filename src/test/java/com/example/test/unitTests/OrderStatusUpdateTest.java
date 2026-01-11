package com.example.test.unitTests;

import com.example.test.models.entities.courier.Courier;
import com.example.test.models.entities.enums.OrderStatus;
import com.example.test.models.entities.enums.PaymentMethod;
import com.example.test.models.entities.enums.PaymentStatus;
import com.example.test.models.entities.order.Order;
import com.example.test.models.entities.payment.Payment;
import com.example.test.models.entities.user.User;
import com.example.test.models.mappers.orderMapper.OrderMapper;
import com.example.test.repositories.orderRepo.OrderRepository;
import com.example.test.repositories.paymentRepo.PaymentRepository;
import com.example.test.services.notiService.NotificationServiceImpl;
import com.example.test.services.orderService.impl.OrderServiceImpl;
import com.example.test.services.userService.impl.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Update Order Status Unit Tests")
class OrderStatusUpdateTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private NotificationServiceImpl notificationService;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private User testUser;
    private Authentication auth;
    private UserDetailsImpl principal;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).build();
        testOrder = new Order();
        testOrder.setId(100L);
        testOrder.setUser(testUser);
        testOrder.setCreatedAt(LocalDateTime.now().minusMinutes(40));
        testOrder.setPaymentMethod(PaymentMethod.CASH);
        testOrder.setStatus(OrderStatus.CREATED);

        principal = mock(UserDetailsImpl.class);
        given(principal.getUser()).willReturn(testUser);
        auth = mock(Authentication.class);
        given(auth.getPrincipal()).willReturn(principal);
    }

    @Test
    @DisplayName("Courier: Success set DELIVERED and update payment")
    void updateStatus_Courier_Success() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_COURIER")))
                .when(auth).getAuthorities();

        Courier courier = new Courier();
        courier.setUser(testUser);
        testOrder.setCourier(courier);

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);

        given(orderRepository.findById(100L)).willReturn(Optional.of(testOrder));
        given(paymentRepository.findByOrderId(100L)).willReturn(Optional.of(payment));


        orderService.updateStatus(auth, 100L, OrderStatus.DELIVERED);


        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(testOrder.getCookTime()).isGreaterThanOrEqualTo(40);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);

        verify(paymentRepository).save(payment);
        verify(notificationService, times(2)).createOrderNotification(eq(1L), anyString(), any());
    }

    @Test
    @DisplayName("Courier: Throw Exception when status is not DELIVERED")
    void updateStatus_Courier_WrongStatus_ThrowsException() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_COURIER")))
                .when(auth).getAuthorities();
        given(orderRepository.findById(100L)).willReturn(Optional.of(testOrder));


        assertThatThrownBy(() -> orderService.updateStatus(auth, 100L, OrderStatus.CONFIRMED))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Courier can only set DELIVERED status");
    }

    @Test
    @DisplayName("Owner: Block manual confirmation for CARD payment")
    void updateStatus_Owner_CardPayment_ThrowsException() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_RESTAURANT_OWNER")))
                .when(auth).getAuthorities();
        testOrder.setPaymentMethod(PaymentMethod.CARD);

        given(orderRepository.findById(100L)).willReturn(Optional.of(testOrder));


        assertThatThrownBy(() -> orderService.updateStatus(auth, 100L, OrderStatus.CONFIRMED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be confirmed manually");
    }

    @Test
    @DisplayName("Notification: Check delayed review notification")
    void updateStatus_Delivered_CreatesDelayedNotification() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(auth).getAuthorities();
        given(orderRepository.findById(100L)).willReturn(Optional.of(testOrder));


        orderService.updateStatus(auth, 100L, OrderStatus.DELIVERED);


        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(notificationService, times(2))
                .createOrderNotification(eq(1L), anyString(), timeCaptor.capture());

        LocalDateTime delayedTime = timeCaptor.getAllValues().get(1);
        assertThat(delayedTime).isAfter(LocalDateTime.now().plusMinutes(29));
    }
}

