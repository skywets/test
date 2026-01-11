package com.example.test.unitTests;

import com.example.test.models.entities.courier.Courier;
import com.example.test.models.entities.enums.OrderStatus;
import com.example.test.models.entities.restaurant.Restaurant;
import com.example.test.repositories.courierRepo.CourierRepository;
import com.example.test.repositories.orderRepo.OrderRepository;
import com.example.test.services.etaService.RestaurantPrepTimeService;
import com.example.test.services.etaService.etaConfig.EtaCourierProperties;
import com.example.test.services.etaService.impl.EtaCalculationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ETA Calculation Service Logic Tests")
class EtaCalculationServiceImplTest {

    @Mock
    private CourierRepository courierRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private RestaurantPrepTimeService prepTimeService;
    @Mock
    private EtaCourierProperties props;

    @InjectMocks
    private EtaCalculationServiceImpl etaService;

    private Restaurant testRestaurant;

    @BeforeEach
    void setUp() {
        testRestaurant = new Restaurant();
        testRestaurant.setId(1L);
        testRestaurant.setName("Gourmet Pizza");

        lenient().when(props.getBaseTimeMinutes()).thenReturn(15);
        lenient().when(props.getNoCourierMultiplier()).thenReturn(3);
    }

    @Test
    @DisplayName("Calculate ETA: High delay when no couriers available")
    void calculateEta_NoCouriers_ReturnsMaxTime() {
        when(prepTimeService.calculateP80PrepTime(testRestaurant)).thenReturn(20);
        when(courierRepository.findByAvailableTrue()).thenReturn(List.of());

        int eta = etaService.calculateEtaMinutes(testRestaurant);


        assertThat(eta).isEqualTo(65);
        verify(orderRepository, never()).countByCourierIdAndStatusIn(any(), any());
    }

    @Test
    @DisplayName("Calculate ETA: Average load with multiple couriers")
    void calculateEta_MultipleCouriers_CalculatesAverageLoad() {
        when(prepTimeService.calculateP80PrepTime(testRestaurant)).thenReturn(15);

        Courier c1 = new Courier();
        c1.setId(10L);
        Courier c2 = new Courier();
        c2.setId(11L);
        Courier c3 = new Courier();
        c3.setId(12L);
        when(courierRepository.findByAvailableTrue()).thenReturn(List.of(c1, c2, c3));
        when(orderRepository.countByCourierIdAndStatusIn(eq(10L), any())).thenReturn(2L);
        when(orderRepository.countByCourierIdAndStatusIn(eq(11L), any())).thenReturn(4L);
        when(orderRepository.countByCourierIdAndStatusIn(eq(12L), any())).thenReturn(9L);

        int eta = etaService.calculateEtaMinutes(testRestaurant);


        assertThat(eta).isEqualTo(90);
    }

    @Test
    @DisplayName("Calculate ETA: Minimum wait time when couriers are idle")
    void calculateEta_IdleCouriers_ReturnsBaseTime() {
        when(prepTimeService.calculateP80PrepTime(testRestaurant)).thenReturn(10);

        Courier c1 = new Courier();
        c1.setId(10L);
        when(courierRepository.findByAvailableTrue()).thenReturn(List.of(c1));
        when(orderRepository.countByCourierIdAndStatusIn(anyLong(), any())).thenReturn(0L);

        int eta = etaService.calculateEtaMinutes(testRestaurant);


        assertThat(eta).isEqualTo(25);
    }

    @Test
    @DisplayName("Verify OrderStatus Filtering Logic")
    void calculateEta_UsesActiveStatusesOnly() {
        when(courierRepository.findByAvailableTrue()).thenReturn(List.of(new Courier()));


        etaService.calculateEtaMinutes(testRestaurant);

        verify(orderRepository).countByCourierIdAndStatusIn(any(), argThat(list ->
                list.contains(OrderStatus.IN_DELIVERY) &&
                        !list.contains(OrderStatus.DELIVERED) &&
                        !list.contains(OrderStatus.CANCELLED)
        ));
    }
}

