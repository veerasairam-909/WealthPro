package com.reviewservice.service;

import com.reviewservice.dto.request.ReviewRequest;
import com.reviewservice.dto.response.ReviewResponse;
import com.reviewservice.entity.Review;
import com.reviewservice.exception.ResourceNotFoundException;
import com.reviewservice.feign.NotificationFeignClient;
import com.reviewservice.feign.PborFeignClient;
import com.reviewservice.feign.WealthproFeignClient;
import com.reviewservice.feign.dto.AccountDTO;
import com.reviewservice.feign.dto.NotificationRequestDTO;
import com.reviewservice.repository.ReviewRepository;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final PborFeignClient pborFeignClient;
    private final WealthproFeignClient wealthproFeignClient;
    private final NotificationFeignClient notificationFeignClient;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             PborFeignClient pborFeignClient,
                             WealthproFeignClient wealthproFeignClient,
                             NotificationFeignClient notificationFeignClient) {
        this.reviewRepository = reviewRepository;
        this.pborFeignClient = pborFeignClient;
        this.wealthproFeignClient = wealthproFeignClient;
        this.notificationFeignClient = notificationFeignClient;
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

        // ── Feign: Validate account exists in PBOR service ──────────────────
        AccountDTO account = null;
        try {
            account = pborFeignClient.getAccountById(request.getAccountId());
            log.info("[FEIGN] Account validated from PBOR-SERVICE → id={}, clientId={}, type={}",
                    account.getAccountId(), account.getClientId(), account.getAccountType());
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Account", request.getAccountId());
        }
        // ────────────────────────────────────────────────────────────────────

        // ── Feign: Fetch client name from Wealthpro for enriched logging ─────
        if (account != null) {
            try {
                var client = wealthproFeignClient.getClientById(account.getClientId());
                log.info("[FEIGN] Client info fetched from WEALTHPRO-SERVICE → clientId={}, name={}",
                        client.getClientId(), client.getName());
            } catch (FeignException e) {
                log.warn("[FEIGN] Could not fetch client info from WEALTHPRO-SERVICE: {}", e.getMessage());
            }
        }
        // ────────────────────────────────────────────────────────────────────

        Review review = mapToEntity(request);
        Review saved = reviewRepository.save(review);

        // ── Feign: Notify client that a new review has been created ──────────
        if (account != null) {
            try {
                NotificationRequestDTO notification = new NotificationRequestDTO(
                        account.getClientId(),
                        "A new portfolio review (" + request.getPeriodType() + ") has been created for your account. "
                                + "Review period: " + request.getPeriodStart() + " to " + request.getPeriodEnd(),
                        "Review"
                );
                notificationFeignClient.sendNotification(notification);
                log.info("[FEIGN] Review notification sent to NOTIFICATIONS-SERVICE for clientId={}",
                        account.getClientId());
            } catch (FeignException e) {
                log.warn("[FEIGN] Could not send review notification: {}", e.getMessage());
            }
        }
        // ────────────────────────────────────────────────────────────────────

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
