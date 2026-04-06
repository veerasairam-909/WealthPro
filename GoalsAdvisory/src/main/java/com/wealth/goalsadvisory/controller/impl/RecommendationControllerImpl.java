package com.wealth.goalsadvisory.controller.impl;

import com.wealth.goalsadvisory.controller.RecommendationController;
import com.wealth.goalsadvisory.dto.request.RecommendationRequest;
import com.wealth.goalsadvisory.dto.response.RecommendationResponse;
import com.wealth.goalsadvisory.enums.RecommendationStatus;
import com.wealth.goalsadvisory.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationControllerImpl implements RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping
    @Override
    public ResponseEntity<RecommendationResponse> createRecommendation(
            @Valid @RequestBody RecommendationRequest request) {
        return new ResponseEntity<>(
                recommendationService.createRecommendation(request),
                HttpStatus.CREATED);
    }

    @GetMapping
    @Override
    public ResponseEntity<List<RecommendationResponse>> getAllRecommendations() {
        return ResponseEntity.ok(recommendationService.getAllRecommendations());
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<RecommendationResponse> getRecommendationById(
            @PathVariable Long id) {
        return ResponseEntity.ok(recommendationService.getRecommendationById(id));
    }

    @GetMapping("/client/{clientId}")
    @Override
    public ResponseEntity<List<RecommendationResponse>> getRecommendationsByClientId(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(
                recommendationService.getRecommendationsByClientId(clientId));
    }

    @GetMapping("/client/{clientId}/status/{status}")
    @Override
    public ResponseEntity<List<RecommendationResponse>> getRecommendationsByClientIdAndStatus(
            @PathVariable Long clientId,
            @PathVariable RecommendationStatus status) {
        return ResponseEntity.ok(
                recommendationService.getRecommendationsByClientIdAndStatus(
                        clientId, status));
    }

    @GetMapping("/model/{modelId}")
    @Override
    public ResponseEntity<List<RecommendationResponse>> getRecommendationsByModelId(
            @PathVariable Long modelId) {
        return ResponseEntity.ok(
                recommendationService.getRecommendationsByModelId(modelId));
    }

    @PatchMapping("/{id}/status")
    @Override
    public ResponseEntity<RecommendationResponse> updateRecommendationStatus(
            @PathVariable Long id,
            @RequestBody RecommendationStatus status) {
        return ResponseEntity.ok(
                recommendationService.updateRecommendationStatus(id, status));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<RecommendationResponse> updateRecommendation(
            @PathVariable Long id,
            @Valid @RequestBody RecommendationRequest request) {
        return ResponseEntity.ok(
                recommendationService.updateRecommendation(id, request));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<String> deleteRecommendation(@PathVariable Long id) {
        recommendationService.deleteRecommendation(id);
        return ResponseEntity.ok(
                "Recommendation deleted successfully with id: " + id);
    }
}