package com.example.test.unitTests;

import com.example.test.models.entities.restaurant.Restaurant;
import com.example.test.repositories.resRepo.RestaurantRepository;
import com.example.test.services.etaService.impl.RestaurantPrepTimeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Restaurant PrepTime Service (P80 Logic) Tests")
class RestaurantPrepTimeServiceImplTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantPrepTimeServiceImpl prepTimeService;

    private Restaurant testRestaurant;

    @BeforeEach
    void setUp() {
        testRestaurant = new Restaurant();
        testRestaurant.setId(1L);
        testRestaurant.setAvgCookingTimeMinutes(25);
    }

    @Test
    @DisplayName("Calculate P80: Should return default avg time when history is empty")
    void calculateP80_EmptyHistory_ReturnsDefaultAvg() {
        when(restaurantRepository.prepTimeHistoryMinutes(eq(1L), any(Pageable.class)))
                .thenReturn(List.of());


        int result = prepTimeService.calculateP80PrepTime(testRestaurant);


        assertThat(result).isEqualTo(25);
    }

    @Test
    @DisplayName("Calculate P80: Should return 80th percentile correctly")
    void calculateP80_WithHistory_ReturnsP80Value() {

        List<Integer> history = List.of(60, 10, 45, 12, 30, 15, 25, 18, 22, 20);

        when(restaurantRepository.prepTimeHistoryMinutes(eq(1L), any(Pageable.class)))
                .thenReturn(history);

        int result = prepTimeService.calculateP80PrepTime(testRestaurant);

        assertThat(result).isEqualTo(30);
    }

    @Test
    @DisplayName("Calculate P80: Single value history")
    void calculateP80_SingleValue_ReturnsThatValue() {
        when(restaurantRepository.prepTimeHistoryMinutes(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(40));


        int result = prepTimeService.calculateP80PrepTime(testRestaurant);


        assertThat(result).isEqualTo(40);
    }

    @Test
    @DisplayName("Verify Pageable parameter: Should request top 100 records")
    void calculateP80_RequestsCorrectPageable() {
        when(restaurantRepository.prepTimeHistoryMinutes(any(), any())).thenReturn(List.of(20));
        prepTimeService.calculateP80PrepTime(testRestaurant);
        verify(restaurantRepository).prepTimeHistoryMinutes(
                eq(1L),
                argThat(pageable -> pageable.getPageSize() == 100 && pageable.getPageNumber() == 0)
        );
    }
}

