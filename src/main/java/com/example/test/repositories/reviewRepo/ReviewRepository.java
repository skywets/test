package com.example.test.repositories.reviewRepo;

import com.example.test.models.entities.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findAllByRestaurantId(Long restaurantId);

    boolean existsByUserIdAndTextAndCreatedAtAfter(Long userId, String text, LocalDateTime time);

}
