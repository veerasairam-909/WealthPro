package com.reviewservice.service;

import com.reviewservice.dto.request.ReviewRequest;
import com.reviewservice.dto.response.ReviewResponse;
import com.reviewservice.entity.Review;
import com.reviewservice.enums.PeriodType;
import com.reviewservice.enums.ReviewStatus;
import com.reviewservice.exception.ResourceNotFoundException;
import com.reviewservice.feign.NotificationFeignClient;
import com.reviewservice.feign.PborFeignClient;
import com.reviewservice.feign.WealthproFeignClient;
import com.reviewservice.feign.dto.AccountDTO;
import com.reviewservice.feign.dto.ClientDTO;
import com.reviewservice.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private PborFeignClient pborFeignClient;

    @Mock
    private WealthproFeignClient wealthproFeignClient;

    @Mock
    private NotificationFeignClient notificationFeignClient;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review review;
    private ReviewRequest reviewRequest;

    @BeforeEach
    void setUp() {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setClientId(1L);
        lenient().when(pborFeignClient.getAccountById(anyLong())).thenReturn(accountDTO);
        lenient().when(wealthproFeignClient.getClientById(anyLong())).thenReturn(new ClientDTO());

        review = Review.builder()
                .reviewId(1L)
                .accountId(101L)
                .periodStart(LocalDate.of(2024, 1, 1))
                .periodEnd(LocalDate.of(2024, 3, 31))
                .periodType(PeriodType.QUARTERLY)
                .highlightsJson("{\"growth\": \"5%\"}")
                .reviewedBy("John Advisor")
                .reviewDate(LocalDate.of(2024, 4, 1))
                .status(ReviewStatus.COMPLETED)
                .build();

        reviewRequest = ReviewRequest.builder()
                .accountId(101L)
                .periodStart(LocalDate.of(2024, 1, 1))
                .periodEnd(LocalDate.of(2024, 3, 31))
                .periodType(PeriodType.QUARTERLY)
                .highlightsJson("{\"growth\": \"5%\"}")
                .reviewedBy("John Advisor")
                .reviewDate(LocalDate.of(2024, 4, 1))
                .status(ReviewStatus.COMPLETED)
                .build();
    }

    @Test
    @DisplayName("Should create a review successfully")
    void testCreateReview() {
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse result = reviewService.createReview(reviewRequest);

        assertNotNull(result);
        assertEquals(101L, result.getAccountId());
        assertEquals(ReviewStatus.COMPLETED, result.getStatus());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Should return review by ID")
    void testGetReviewById() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ReviewResponse result = reviewService.getReviewById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getReviewId());
        assertEquals(101L, result.getAccountId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when review not found")
    void testGetReviewById_NotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewById(99L));
    }

    @Test
    @DisplayName("Should return all reviews")
    void testGetAllReviews() {
        when(reviewRepository.findAll()).thenReturn(List.of(review));

        List<ReviewResponse> result = reviewService.getAllReviews();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(101L, result.get(0).getAccountId());
    }

    @Test
    @DisplayName("Should update review successfully")
    void testUpdateReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse result = reviewService.updateReview(1L, reviewRequest);

        assertNotNull(result);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Should delete review successfully")
    void testDeleteReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        reviewService.deleteReview(1L);

        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException on delete when review not found")
    void testDeleteReview_NotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.deleteReview(99L));
    }
}
