package com.reviewservice.service;

import com.reviewservice.dto.request.ReviewRequest;
import com.reviewservice.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse createReview(ReviewRequest request);

    ReviewResponse getReviewById(Long reviewId);

    List<ReviewResponse> getAllReviews();

    List<ReviewResponse> getReviewsByAccountId(Long accountId);

    ReviewResponse updateReview(Long reviewId, ReviewRequest request);

    void deleteReview(Long reviewId);
}
