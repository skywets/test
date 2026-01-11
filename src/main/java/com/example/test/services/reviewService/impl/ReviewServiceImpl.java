package com.example.test.services.reviewService.impl;

import com.example.test.exceptions.NotFoundException;
import com.example.test.models.dtos.review.CreateReviewDto;
import com.example.test.models.dtos.review.ReviewDto;
import com.example.test.models.dtos.review.UpdateReviewDto;
import com.example.test.models.entities.enums.OrderStatus;
import com.example.test.models.entities.order.Order;
import com.example.test.models.entities.review.Review;
import com.example.test.models.mappers.review.ReviewMapper;
import com.example.test.repositories.orderRepo.OrderRepository;
import com.example.test.repositories.reviewRepo.ReviewRepository;
import com.example.test.services.reviewService.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ReviewMapper reviewMapper;

    private static final List<String> STOP_WORDS = List.of("спам", "реклама", "мат1", "мат2");

    @Override
    public ReviewDto createReview(Long userId, CreateReviewDto dto) {
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You can only review your own orders");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("You can only review delivered orders");
        }

        validateText(userId, dto.getText());

        Review review = new Review();
        review.setText(dto.getText());
        review.setGrade(dto.getGrade());
        review.setOrder(order);
        review.setUser(order.getUser());
        review.setRestaurant(order.getRestaurant());

        return reviewMapper.toDto(reviewRepository.save(review));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> getByRestaurant(Long restaurantId) {
        return reviewRepository.findAllByRestaurantId(restaurantId).stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    @Override
    public ReviewDto update(Long userId, boolean isAdmin, Long reviewId, UpdateReviewDto dto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found"));

        validateReviewOwnership(review, userId, isAdmin);

        validateText(userId, dto.getText());

        review.setText(dto.getText());
        review.setGrade(dto.getGrade());

        return reviewMapper.toDto(reviewRepository.save(review));
    }

    @Override
    public void delete(Long userId, boolean isAdmin, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found"));

        validateReviewOwnership(review, userId, isAdmin);

        reviewRepository.delete(review);
    }


    private void validateReviewOwnership(Review review, Long userId, boolean isAdmin) {
        if (!isAdmin && !review.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to modify this review");
        }
    }

    private void validateText(Long userId, String text) {
        String lowerText = text.toLowerCase();

        if (STOP_WORDS.stream().anyMatch(lowerText::contains)) {
            throw new IllegalArgumentException("Review contains forbidden words");
        }


        boolean isDuplicate = reviewRepository.existsByUserIdAndTextAndCreatedAtAfter(
                userId, text, LocalDateTime.now().minusDays(1)
        );
        if (isDuplicate) {
            throw new IllegalStateException("You already posted an identical review recently");
        }
    }
}
