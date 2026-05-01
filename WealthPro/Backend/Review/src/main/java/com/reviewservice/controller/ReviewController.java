package com.reviewservice.controller;

import com.reviewservice.dto.request.ReviewRequest;
import com.reviewservice.dto.response.ReviewResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ReviewController {

    ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request,
                                                String username, String roles, Long clientId);

    ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long reviewId,
                                                 String username, String roles, Long clientId);

    ResponseEntity<List<ReviewResponse>> getAllReviews(String username, String roles, Long clientId);

    ResponseEntity<List<ReviewResponse>> getReviewsByAccountId(@PathVariable Long accountId,
                                                               String username, String roles, Long clientId);

    ResponseEntity<ReviewResponse> updateReview(@PathVariable Long reviewId,
                                                @Valid @RequestBody ReviewRequest request,
                                                String username, String roles, Long clientId);

    ResponseEntity<Void> deleteReview(@PathVariable Long reviewId,
                                      String username, String roles, Long clientId);
}
