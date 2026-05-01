package com.reviewservice.controller;

import com.reviewservice.dto.request.ReviewRequest;
import com.reviewservice.dto.response.ReviewResponse;
import com.reviewservice.security.AuthContext;
import com.reviewservice.security.OwnershipGuard;
import com.reviewservice.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewControllerImpl implements ReviewController {

    private final ReviewService reviewService;
    private final OwnershipGuard ownershipGuard;

    public ReviewControllerImpl(ReviewService reviewService, OwnershipGuard ownershipGuard) {
        this.reviewService = reviewService;
        this.ownershipGuard = ownershipGuard;
    }

    @Override
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            @RequestHeader(value = AuthContext.HDR_USERNAME, required = false) String username,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long clientId) {
        AuthContext ctx = new AuthContext(username, roles, clientId);
        if (ctx.isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clients cannot create reviews.");
        }
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewById(
            @PathVariable Long reviewId,
            @RequestHeader(value = AuthContext.HDR_USERNAME, required = false) String username,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long clientId) {
        AuthContext ctx = new AuthContext(username, roles, clientId);
        ReviewResponse response = reviewService.getReviewById(reviewId);
        ownershipGuard.checkAccount(ctx, response.getAccountId());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getAllReviews(
            @RequestHeader(value = AuthContext.HDR_USERNAME, required = false) String username,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long clientId) {
        AuthContext ctx = new AuthContext(username, roles, clientId);
        if (ctx.isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clients cannot list all reviews.");
        }
        List<ReviewResponse> responses = reviewService.getAllReviews();
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByAccountId(
            @PathVariable Long accountId,
            @RequestHeader(value = AuthContext.HDR_USERNAME, required = false) String username,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long clientId) {
        AuthContext ctx = new AuthContext(username, roles, clientId);
        ownershipGuard.checkAccount(ctx, accountId);
        List<ReviewResponse> responses = reviewService.getReviewsByAccountId(accountId);
        return ResponseEntity.ok(responses);
    }

    @Override
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request,
            @RequestHeader(value = AuthContext.HDR_USERNAME, required = false) String username,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long clientId) {
        AuthContext ctx = new AuthContext(username, roles, clientId);
        if (ctx.isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clients cannot update reviews.");
        }
        ReviewResponse response = reviewService.updateReview(reviewId, request);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader(value = AuthContext.HDR_USERNAME, required = false) String username,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long clientId) {
        AuthContext ctx = new AuthContext(username, roles, clientId);
        if (ctx.isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clients cannot delete reviews.");
        }
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
