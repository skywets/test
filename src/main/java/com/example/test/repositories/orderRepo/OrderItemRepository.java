package com.example.test.repositories.orderRepo;

import com.example.test.models.entities.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {


}
