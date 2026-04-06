package com.wealth.goalsadvisory.service;

import com.wealth.goalsadvisory.dto.request.RecommendationRequest;
import com.wealth.goalsadvisory.dto.response.RecommendationResponse;
import com.wealth.goalsadvisory.enums.RecommendationStatus;

import java.util.List;

public interface RecommendationService {

    RecommendationResponse createRecommendation(RecommendationRequest request);

    List<RecommendationResponse> getAllRecommendations();

    RecommendationResponse getRecommendationById(Long id);

    List<RecommendationResponse> getRecommendationsByClientId(Long clientId);

    List<RecommendationResponse> getRecommendationsByClientIdAndStatus(
            Long clientId, RecommendationStatus status);

    List<RecommendationResponse> getRecommendationsByModelId(Long modelId);

    RecommendationResponse updateRecommendationStatus(Long id, RecommendationStatus status);

    RecommendationResponse updateRecommendation(Long id, RecommendationRequest request);

    void deleteRecommendation(Long id);
}