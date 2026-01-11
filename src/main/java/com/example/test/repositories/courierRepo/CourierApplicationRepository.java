package com.example.test.repositories.courierRepo;

import com.example.test.models.entities.courier.CourierApplication;
import com.example.test.models.entities.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CourierApplicationRepository extends JpaRepository<CourierApplication, Long> {

    boolean existsById(Long id);

    boolean existsByUserIdAndStatus(Long userId, Status status);

    List<CourierApplication> findAllByUserId(Long userId);

}
