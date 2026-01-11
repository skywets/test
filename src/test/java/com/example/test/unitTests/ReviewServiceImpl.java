package com.example.test.unitTests;

import com.example.test.models.dtos.review.CreateReviewDto;
import com.example.test.models.dtos.review.ReviewDto;
import com.example.test.models.dtos.review.UpdateReviewDto;
import com.example.test.models.entities.enums.OrderStatus;
import com.example.test.models.entities.order.Order;
import com.example.test.models.entities.review.Review;
import com.example.test.models.entities.user.User;
import com.example.test.models.mappers.review.ReviewMapper;
import com.example.test.repositories.orderRepo.OrderRepository;
import com.example.test.repositories.reviewRepo.ReviewRepository;
import com.example.test.services.reviewService.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Review Service Unit Tests")
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User testUser;
    private Order testOrder;
    private Review testReview;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@test.com").build();

        testOrder = new Order();
        testOrder.setId(100L);
        testOrder.setUser(testUser);
        testOrder.setStatus(OrderStatus.DELIVERED);

        testReview = new Review();
        testReview.setId(500L);
        testReview.setUser(testUser);
        testReview.setText("Great food!");
        testReview.setGrade(5.0);
    }

    @Test
    @DisplayName("Create: Success flow")
    void createReview_Success() {
        CreateReviewDto dto = new CreateReviewDto(100L, "Delicious pizza", 5.0);
        given(orderRepository.findById(100L)).willReturn(Optional.of(testOrder));
        given(reviewRepository.existsByUserIdAndTextAndCreatedAtAfter(eq(1L), anyString(), any())).willReturn(false);
        given(reviewRepository.save(any(Review.class))).willReturn(testReview);
        given(reviewMapper.toDto(any())).willReturn(new ReviewDto());


        reviewService.createReview(1L, dto);


        verify(reviewRepository).save(argThat(review -> {
            assertThat(review.getText()).isEqualTo("Delicious pizza");
            assertThat(review.getUser().getId()).isEqualTo(1L);
            return true;
        }));
    }

    @Test
    @DisplayName("Validation: Throws exception for stop words")
    void createReview_StopWords_ThrowsException() {
        CreateReviewDto dto = new CreateReviewDto(100L, "Это спам и реклама", 1.0);
        given(orderRepository.findById(100L)).willReturn(Optional.of(testOrder));


        assertThatThrownBy(() -> reviewService.createReview(1L, dto)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("forbidden words");
    }

    @Test
    @DisplayName("Validation: Throws exception for duplicate text within 24h")
    void createReview_Duplicate_ThrowsException() {
        CreateReviewDto dto = new CreateReviewDto(100L, "Duplicate text", 5.0);
        given(orderRepository.findById(100L)).willReturn(Optional.of(testOrder));
        given(reviewRepository.existsByUserIdAndTextAndCreatedAtAfter(eq(1L), eq("Duplicate text"), any())).willReturn(true);


        assertThatThrownBy(() -> reviewService.createReview(1L, dto)).isInstanceOf(IllegalStateException.class).hasMessageContaining("already posted an identical review");
    }

    @Test
    @DisplayName("Security: Only owner can review their order")
    void createReview_NotOwner_ThrowsAccessDenied() {
        CreateReviewDto dto = new CreateReviewDto(100L, "Text", 5.0);
        given(orderRepository.findById(100L)).willReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> reviewService.createReview(99L, dto)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Order Status: Cannot review non-delivered orders")
    void createReview_NotDelivered_ThrowsException() {
        testOrder.setStatus(OrderStatus.CREATED);
        CreateReviewDto dto = new CreateReviewDto(100L, "Text", 5.0);
        given(orderRepository.findById(100L)).willReturn(Optional.of(testOrder));


        assertThatThrownBy(() -> reviewService.createReview(1L, dto)).isInstanceOf(IllegalStateException.class).hasMessageContaining("only review delivered orders");
    }

    @Test
    @DisplayName("Update: Admin can update any review")
    void updateReview_AdminSuccess() {
        UpdateReviewDto dto = new UpdateReviewDto("Updated by admin", 3.0);
        given(reviewRepository.findById(500L)).willReturn(Optional.of(testReview));
        given(reviewRepository.save(any())).willReturn(testReview);


        reviewService.update(999L, true, 500L, dto);


        assertThat(testReview.getText()).isEqualTo("Updated by admin");
        verify(reviewRepository).save(testReview);
    }

    @Test
    @DisplayName("Delete: Stranger cannot delete review")
    void deleteReview_Stranger_ThrowsAccessDenied() {
        given(reviewRepository.findById(500L)).willReturn(Optional.of(testReview));
        assertThatThrownBy(() -> reviewService.delete(2L, false, 500L)).isInstanceOf(AccessDeniedException.class).hasMessageContaining("do not have permission");
    }
}
