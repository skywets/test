package com.example.test.repositories.courierRepo;


import com.example.test.models.entities.courier.Courier;
import com.example.test.models.entities.enums.CourierStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {

    boolean existsByUserId(Long userId);
    Optional<Courier> findByUserId(Long userId);

    List<Courier> findByAvailableTrue();

    List<Courier> findAll();
    List<Courier> findByStatus(CourierStatus status);
}
