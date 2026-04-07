package com.reviewservice.repository;

import com.reviewservice.entity.Review;
import com.reviewservice.enums.PeriodType;
import com.reviewservice.enums.ReviewStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    private Review review;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();

        review = Review.builder()
                .accountId(101L)
                .periodStart(LocalDate.of(2024, 1, 1))
                .periodEnd(LocalDate.of(2024, 3, 31))
                .periodType(PeriodType.QUARTERLY)
                .highlightsJson("{\"growth\": \"5%\"}")
                .reviewedBy("John Advisor")
                .reviewDate(LocalDate.of(2024, 4, 1))
                .status(ReviewStatus.COMPLETED)
                .build();

        reviewRepository.save(review);
    }

    @Test
    @DisplayName("Should save and retrieve a review by ID")
    void testSaveAndFindById() {
        Optional<Review> found = reviewRepository.findById(review.getReviewId());
        assertTrue(found.isPresent());
        assertEquals(101L, found.get().getAccountId());
        assertEquals(ReviewStatus.COMPLETED, found.get().getStatus());
    }

    @Test
    @DisplayName("Should find all reviews by account ID")
    void testFindByAccountId() {
        List<Review> reviews = reviewRepository.findByAccountId(101L);
        assertFalse(reviews.isEmpty());
        assertEquals(1, reviews.size());
        assertEquals(101L, reviews.get(0).getAccountId());
    }

    @Test
    @DisplayName("Should find reviews by account ID and status")
    void testFindByAccountIdAndStatus() {
        List<Review> reviews = reviewRepository.findByAccountIdAndStatus(101L, ReviewStatus.COMPLETED);
        assertFalse(reviews.isEmpty());
        assertEquals(ReviewStatus.COMPLETED, reviews.get(0).getStatus());
    }

    @Test
    @DisplayName("Should return true if review exists for account")
    void testExistsByAccountId() {
        assertTrue(reviewRepository.existsByAccountId(101L));
        assertFalse(reviewRepository.existsByAccountId(999L));
    }

    @Test
    @DisplayName("Should delete a review by ID")
    void testDeleteById() {
        reviewRepository.deleteById(review.getReviewId());
        Optional<Review> found = reviewRepository.findById(review.getReviewId());
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should return all reviews")
    void testFindAll() {
        List<Review> all = reviewRepository.findAll();
        assertNotNull(all);
        assertEquals(1, all.size());
    }
}
