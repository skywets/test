package com.example.test.unitTests;

import com.example.test.models.dtos.orderDto.OrderDto;
import com.example.test.models.entities.courier.Courier;
import com.example.test.models.entities.enums.CourierStatus;
import com.example.test.models.entities.enums.OrderStatus;
import com.example.test.models.entities.order.Order;
import com.example.test.models.mappers.orderMapper.OrderMapper;
import com.example.test.repositories.courierRepo.CourierRepository;
import com.example.test.repositories.orderRepo.OrderRepository;
import com.example.test.services.courierService.CourierAssignmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Courier Assignment Service Business Logic Tests")
class CourierAssignmentServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CourierRepository courierRepository;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private CourierAssignmentServiceImpl assignmentService;

    private Courier testCourier;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testCourier = new Courier();
        testCourier.setId(1L);
        testCourier.setStatus(CourierStatus.AVAILABLE);
        testCourier.setAvailable(true);

        testOrder = new Order();
        testOrder.setId(100L);
        testOrder.setStatus(OrderStatus.CONFIRMED);
        testOrder.setCourier(null);
    }

    @Test
    @DisplayName("Manual assignment: Success should update both order and courier statuses")
    void assignOrderToCourier_Success() {
        when(courierRepository.findById(1L)).thenReturn(Optional.of(testCourier));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(testOrder));


        assignmentService.assignOrderToCourier(1L, 100L);

        assertThat(testOrder.getCourier()).isEqualTo(testCourier);
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.IN_DELIVERY);
        assertThat(testCourier.getStatus()).isEqualTo(CourierStatus.WORKING);
        assertThat(testCourier.isAvailable()).isFalse();

        verify(orderRepository).save(testOrder);
        verify(courierRepository).save(testCourier);
    }

    @Test
    @DisplayName("Manual assignment: Should throw exception if courier is not available")
    void assignOrderToCourier_CourierBusy_ThrowsException() {
        testCourier.setStatus(CourierStatus.WORKING);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(testCourier));


        assertThatThrownBy(() -> assignmentService.assignOrderToCourier(1L, 100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Courier is not available");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Manual assignment: Should throw exception if order is already assigned")
    void assignOrderToCourier_OrderAlreadyAssigned_ThrowsException() {
        testOrder.setCourier(new Courier());
        when(courierRepository.findById(1L)).thenReturn(Optional.of(testCourier));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(testOrder));


        assertThatThrownBy(() -> assignmentService.assignOrderToCourier(1L, 100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Order already assigned");
    }

    @Test
    @DisplayName("Scheduled Task: Should assign available couriers to confirmed orders")
    void assignCouriers_SuccessfulBatchAssignment() {
        Order order1 = new Order();
        order1.setId(101L);
        order1.setStatus(OrderStatus.CONFIRMED);
        Order order2 = new Order();
        order2.setId(102L);
        order2.setStatus(OrderStatus.CONFIRMED);

        Courier courier1 = new Courier();
        courier1.setId(10L);
        courier1.setStatus(CourierStatus.AVAILABLE);
        Courier courier2 = new Courier();
        courier2.setId(11L);
        courier2.setStatus(CourierStatus.AVAILABLE);

        when(orderRepository.findByCourierIsNullAndStatus(OrderStatus.CONFIRMED))
                .thenReturn(List.of(order1, order2));
        when(courierRepository.findByStatus(CourierStatus.AVAILABLE))
                .thenReturn(List.of(courier1, courier2));


        assignmentService.assignCouriers();


        assertThat(order1.getCourier()).isEqualTo(courier1);
        assertThat(order2.getCourier()).isEqualTo(courier2);

        assertThat(courier1.getStatus()).isEqualTo(CourierStatus.WORKING);
        assertThat(courier2.isAvailable()).isFalse();

        verify(orderRepository, times(2)).save(any(Order.class));
        verify(courierRepository, times(2)).save(any(Courier.class));
    }

    @Test
    @DisplayName("Scheduled Task: Should stop assigning if couriers run out")
    void assignCouriers_StopWhenNoMoreCouriers() {
        Order order1 = new Order();
        order1.setId(101L);
        Order order2 = new Order();
        order2.setId(102L);
        Courier courier1 = new Courier();
        courier1.setId(10L);
        courier1.setStatus(CourierStatus.AVAILABLE);

        when(orderRepository.findByCourierIsNullAndStatus(any())).thenReturn(List.of(order1, order2));
        when(courierRepository.findByStatus(any())).thenReturn(List.of(courier1));


        assignmentService.assignCouriers();


        assertThat(order1.getCourier()).isEqualTo(courier1);
        assertThat(order2.getCourier()).isNull();
        verify(orderRepository, times(1)).save(order1);
    }

    @Test
    @DisplayName("Get active orders: Success mapping to DTO")
    void getActiveOrders_Success() {
        when(orderRepository.findByCourierIdAndStatusIn(anyLong(), anyList()))
                .thenReturn(List.of(testOrder));
        when(orderMapper.toDto(any())).thenReturn(new OrderDto());


        List<OrderDto> result = assignmentService.getActiveOrders(1L);


        assertThat(result).hasSize(1);
        verify(orderRepository).findByCourierIdAndStatusIn(eq(1L), anyList());
    }
}

