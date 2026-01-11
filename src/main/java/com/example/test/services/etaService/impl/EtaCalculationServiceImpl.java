package com.example.test.services.etaService.impl;

import com.example.test.models.entities.courier.Courier;
import com.example.test.models.entities.enums.OrderStatus;
import com.example.test.models.entities.restaurant.Restaurant;
import com.example.test.repositories.courierRepo.CourierRepository;
import com.example.test.repositories.orderRepo.OrderRepository;
import com.example.test.services.etaService.EtaCalculationService;
import com.example.test.services.etaService.RestaurantPrepTimeService;
import com.example.test.services.etaService.etaConfig.EtaCourierProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EtaCalculationServiceImpl implements EtaCalculationService {

    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;
    private final RestaurantPrepTimeService prepTimeService;
    private final EtaCourierProperties props;

    @Override
    public int calculateEtaMinutes(Restaurant restaurant) {

        int prepTime =
                prepTimeService.calculateP80PrepTime(restaurant);

        List<Courier> couriers =
                courierRepository.findByAvailableTrue();

        if (couriers.isEmpty()) {
            return prepTime
                    + props.getBaseTimeMinutes()
                    * props.getNoCourierMultiplier();
        }

        int totalLoad = 0;

        for (Courier courier : couriers) {

            long activeOrders =
                    orderRepository.countByCourierIdAndStatusIn(
                            courier.getId(),
                            Arrays.stream(OrderStatus.values())
                                    .filter(OrderStatus::isActiveForCourier)
                                    .toList()
                    );

            totalLoad += activeOrders;
        }

        int avgQueue =
                Math.max(1, totalLoad / couriers.size());

        int courierWaitTime =
                props.getBaseTimeMinutes() * avgQueue;

        return prepTime + courierWaitTime;
    }
}
