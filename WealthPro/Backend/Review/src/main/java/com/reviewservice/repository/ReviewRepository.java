package com.reviewservice.repository;

import com.reviewservice.entity.Review;
import com.reviewservice.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Find all reviews for a specific account
    List<Review> findByAccountId(Long accountId);

    // Find all reviews for an account filtered by status
    List<Review> findByAccountIdAndStatus(Long accountId, ReviewStatus status);

    // Check if a review exists for an account
    boolean existsByAccountId(Long accountId);
}
