package com.example.test.services.courierService;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.orderDto.OrderDto;
import com.example.test.models.entities.courier.Courier;
import com.example.test.models.entities.enums.CourierStatus;
import com.example.test.models.entities.enums.OrderStatus;
import com.example.test.models.entities.order.Order;
import com.example.test.models.mappers.orderMapper.OrderMapper;
import com.example.test.repositories.courierRepo.CourierRepository;
import com.example.test.repositories.orderRepo.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourierAssignmentServiceImpl implements CourierAssignmentService {

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final OrderMapper orderMapper;


    @Override
    public void assignOrderToCourier(Long courierId, Long orderId) {

        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new NotFoundException("Courier not found"));

        if (courier.getStatus() != CourierStatus.AVAILABLE) {
            throw new IllegalStateException("Courier is not available");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (order.getCourier() != null) {
            throw new IllegalStateException("Order already assigned");
        }

        order.setCourier(courier);
        order.setStatus(OrderStatus.IN_DELIVERY);

        courier.setStatus(CourierStatus.WORKING);
        courier.setAvailable(false);

        orderRepository.save(order);
        courierRepository.save(courier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getActiveOrders(Long courierId) {

        return orderRepository
                .findByCourierIdAndStatusIn(
                        courierId,
                        List.of(
                                OrderStatus.CONFIRMED,
                                OrderStatus.IN_DELIVERY
                        )
                )
                .stream()
                .map(orderMapper::toDto)
                .toList();

    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void assignCouriers() {

        List<Order> orders = orderRepository.findByCourierIsNullAndStatus(OrderStatus.CONFIRMED);

        if (orders.isEmpty()) {
            return;
        }

        List<Courier> couriers = courierRepository.findByStatus(CourierStatus.AVAILABLE);

        if (couriers.isEmpty()) {
            log.info("No available couriers");
            return;
        }

        Iterator<Courier> courierIterator = couriers.iterator();

        for (Order order : orders) {

            if (!courierIterator.hasNext()) {
                log.info("Not enough couriers for remaining orders");
                break;
            }

            Courier courier = courierIterator.next();

            order.setCourier(courier);

            courier.setStatus(CourierStatus.WORKING);
            courier.setAvailable(false);

            orderRepository.save(order);
            courierRepository.save(courier);

            log.info(
                    "Courier {} assigned to order {}",
                    courier.getId(),
                    order.getId()
            );
        }
    }
}