package com.example.test.repositories.orderRepo;

import com.example.test.models.entities.enums.OrderStatus;
import com.example.test.models.entities.order.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    List<Order> findByUserId(Long userId);

    long countByCourierIdAndStatusIn(Long courierId, Collection<OrderStatus> statuses);

    List<Order> findByCourierIdAndStatusIn(Long courierId, Collection<OrderStatus> statuses);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.courier IS NULL AND o.status = :status")
    List<Order> findByCourierIsNullAndStatus(@Param("status") OrderStatus status);


}
