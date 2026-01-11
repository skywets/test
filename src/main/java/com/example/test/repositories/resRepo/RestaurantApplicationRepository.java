package com.example.test.repositories.resRepo;

import com.example.test.models.entities.enums.Status;
import com.example.test.models.entities.restaurant.RestaurantApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantApplicationRepository extends JpaRepository<RestaurantApplication, Long> {

    boolean existsByUserIdAndStatus(Long userId, Status status);

    List<RestaurantApplication> findAllByUserId(Long userId);

    Optional<RestaurantApplication> findFirstByUserIdAndStatusAndRestaurantCreatedFalse(
            Long userId,
            Status status
    );

}
