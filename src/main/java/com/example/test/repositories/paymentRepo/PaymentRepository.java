package com.example.test.repositories.paymentRepo;

import com.example.test.models.entities.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByOrderId(Long orderId);

    Optional<Payment> findByOrderId(Long orderId);


}
