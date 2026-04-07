package com.reviewservice.service;

import com.reviewservice.dto.request.ReviewRequest;
import com.reviewservice.dto.response.ReviewResponse;
import com.reviewservice.entity.Review;
import com.reviewservice.exception.ResourceNotFoundException;
import com.reviewservice.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    // -------------------------------------------------------
    // Mapping Methods
    // -------------------------------------------------------

    private Review mapToEntity(ReviewRequest request) {
        Review review = new Review();
        review.setAccountId(request.getAccountId());
        review.setPeriodStart(request.getPeriodStart());
        review.setPeriodEnd(request.getPeriodEnd());
        review.setPeriodType(request.getPeriodType());
        review.setHighlightsJson(request.getHighlightsJson());
        review.setReviewedBy(request.getReviewedBy());
        review.setReviewDate(request.getReviewDate());
        review.setStatus(request.getStatus());
        return review;
    }

    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setReviewId(review.getReviewId());
        response.setAccountId(review.getAccountId());
        response.setPeriodStart(review.getPeriodStart());
        response.setPeriodEnd(review.getPeriodEnd());
        response.setPeriodType(review.getPeriodType());
        response.setHighlightsJson(review.getHighlightsJson());
        response.setReviewedBy(review.getReviewedBy());
        response.setReviewDate(review.getReviewDate());
        response.setStatus(review.getStatus());
        return response;
    }

    // -------------------------------------------------------
    // Service Methods
    // -------------------------------------------------------

    @Override
    public ReviewResponse createReview(ReviewRequest request) {
        Review review = mapToEntity(request);
        Review saved = reviewRepository.save(review);
        return mapToResponse(saved);
    }

    @Override
    public ReviewResponse getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));
        return mapToResponse(review);
    }

    @Override
    public List<ReviewResponse> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll();
        List<ReviewResponse> responses = new ArrayList<>();
        for (Review review : reviews) {
            responses.add(mapToResponse(review));
        }
        return responses;
    }

    @Override
    public List<ReviewResponse> getReviewsByAccountId(Long accountId) {
        List<Review> reviews = reviewRepository.findByAccountId(accountId);
        List<ReviewResponse> responses = new ArrayList<>();
        for (Review review : reviews) {
            responses.add(mapToResponse(review));
        }
        return responses;
    }

    @Override
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request) {
        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        existing.setAccountId(request.getAccountId());
        existing.setPeriodStart(request.getPeriodStart());
        existing.setPeriodEnd(request.getPeriodEnd());
        existing.setPeriodType(request.getPeriodType());
        existing.setHighlightsJson(request.getHighlightsJson());
        existing.setReviewedBy(request.getReviewedBy());
        existing.setReviewDate(request.getReviewDate());
        existing.setStatus(request.getStatus());

        Review updated = reviewRepository.save(existing);
        return mapToResponse(updated);
    }

    @Override
    public void deleteReview(Long reviewId) {
        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));
        reviewRepository.delete(existing);
    }
}