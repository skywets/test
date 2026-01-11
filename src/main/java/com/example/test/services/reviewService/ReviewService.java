package com.example.test.services.reviewService;

import com.example.test.models.dtos.review.CreateReviewDto;
import com.example.test.models.dtos.review.ReviewDto;
import com.example.test.models.dtos.review.UpdateReviewDto;

import java.util.List;

public interface ReviewService {

    ReviewDto createReview(Long userId, CreateReviewDto dto);

    List<ReviewDto> getByRestaurant(Long restaurantId);

    ReviewDto update(Long reviewId, boolean isAdmin, Long userId, UpdateReviewDto dto);

    void delete(Long reviewId, boolean isAdmin, Long userId);
}

