package com.reviewservice.controller;

import com.reviewservice.dto.request.ReviewRequest;
import com.reviewservice.dto.response.ReviewResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ReviewController {

    ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request);

    ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long reviewId);

    ResponseEntity<List<ReviewResponse>> getAllReviews();

    ResponseEntity<List<ReviewResponse>> getReviewsByAccountId(@PathVariable Long accountId);

    ResponseEntity<ReviewResponse> updateReview(@PathVariable Long reviewId,
                                                @Valid @RequestBody ReviewRequest request);

    ResponseEntity<Void> deleteReview(@PathVariable Long reviewId);
}
