package com.wealth.goalsadvisory.controller;

import com.wealth.goalsadvisory.dto.request.RecommendationRequest;
import com.wealth.goalsadvisory.dto.response.RecommendationResponse;
import com.wealth.goalsadvisory.enums.RecommendationStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;
public interface RecommendationController {

    ResponseEntity<RecommendationResponse> createRecommendation(
            RecommendationRequest request);

    ResponseEntity<List<RecommendationResponse>> getAllRecommendations();

    ResponseEntity<RecommendationResponse> getRecommendationById(Long id);

    ResponseEntity<List<RecommendationResponse>> getRecommendationsByClientId(
            Long clientId);

    ResponseEntity<List<RecommendationResponse>> getRecommendationsByClientIdAndStatus(
            Long clientId, RecommendationStatus status);

    ResponseEntity<List<RecommendationResponse>> getRecommendationsByModelId(
            Long modelId);

    ResponseEntity<RecommendationResponse> updateRecommendationStatus(
            Long id, RecommendationStatus status);

    ResponseEntity<RecommendationResponse> updateRecommendation(
            Long id, RecommendationRequest request);

    ResponseEntity<String> deleteRecommendation(Long id);
}