package com.wealth.goalsadvisory.controller.impl;

import com.wealth.goalsadvisory.controller.RecommendationController;
import com.wealth.goalsadvisory.dto.request.RecommendationRequest;
import com.wealth.goalsadvisory.dto.response.RecommendationResponse;
import com.wealth.goalsadvisory.enums.RecommendationStatus;
import com.wealth.goalsadvisory.security.AuthContext;
import com.wealth.goalsadvisory.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationControllerImpl implements RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping
    @Override
    public ResponseEntity<RecommendationResponse> createRecommendation(
            @Valid @RequestBody RecommendationRequest request,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient() && !ctx.ownsClient(request.getClientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Clients can only create recommendations for themselves");
        }
        return new ResponseEntity<>(
                recommendationService.createRecommendation(request),
                HttpStatus.CREATED);
    }

    @GetMapping
    @Override
    public ResponseEntity<List<RecommendationResponse>> getAllRecommendations(
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Clients cannot list all recommendations; use /client/{yourClientId}");
        }
        return ResponseEntity.ok(recommendationService.getAllRecommendations());
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<RecommendationResponse> getRecommendationById(
            @PathVariable Long id,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        RecommendationResponse rec = recommendationService.getRecommendationById(id);
        if (ctx.isClient() && !ctx.ownsClient(rec.getClientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have access to this recommendation");
        }
        return ResponseEntity.ok(rec);
    }

    @GetMapping("/client/{clientId}")
    @Override
    public ResponseEntity<List<RecommendationResponse>> getRecommendationsByClientId(
            @PathVariable Long clientId,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient() && !ctx.ownsClient(clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only view your own recommendations");
        }
        return ResponseEntity.ok(
                recommendationService.getRecommendationsByClientId(clientId));
    }

    @GetMapping("/client/{clientId}/status/{status}")
    @Override
    public ResponseEntity<List<RecommendationResponse>> getRecommendationsByClientIdAndStatus(
            @PathVariable Long clientId,
            @PathVariable RecommendationStatus status,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient() && !ctx.ownsClient(clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only view your own recommendations");
        }
        return ResponseEntity.ok(
                recommendationService.getRecommendationsByClientIdAndStatus(
                        clientId, status));
    }

    @GetMapping("/model/{modelId}")
    @Override
    public ResponseEntity<List<RecommendationResponse>> getRecommendationsByModelId(
            @PathVariable Long modelId,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Clients cannot list recommendations by model portfolio");
        }
        return ResponseEntity.ok(
                recommendationService.getRecommendationsByModelId(modelId));
    }

    @PatchMapping("/{id}/status")
    @Override
    public ResponseEntity<RecommendationResponse> updateRecommendationStatus(
            @PathVariable Long id,
            @RequestBody RecommendationStatus status,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        RecommendationResponse existing = recommendationService.getRecommendationById(id);
        if (ctx.isClient() && !ctx.ownsClient(existing.getClientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You cannot modify another client's recommendation");
        }
        return ResponseEntity.ok(
                recommendationService.updateRecommendationStatus(id, status));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<RecommendationResponse> updateRecommendation(
            @PathVariable Long id,
            @Valid @RequestBody RecommendationRequest request,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        RecommendationResponse existing = recommendationService.getRecommendationById(id);
        if (ctx.isClient() && !ctx.ownsClient(existing.getClientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You cannot modify another client's recommendation");
        }
        return ResponseEntity.ok(
                recommendationService.updateRecommendation(id, request));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<String> deleteRecommendation(
            @PathVariable Long id,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        RecommendationResponse existing = recommendationService.getRecommendationById(id);
        if (ctx.isClient() && !ctx.ownsClient(existing.getClientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You cannot delete another client's recommendation");
        }
        recommendationService.deleteRecommendation(id);
        return ResponseEntity.ok(
                "Recommendation deleted successfully with id: " + id);
    }
}
