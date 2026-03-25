package com.wealth.goalsadvisory.service.Impl;

import com.wealth.goalsadvisory.dto.request.RecommendationRequest;
import com.wealth.goalsadvisory.dto.response.RecommendationResponse;
import com.wealth.goalsadvisory.entity.ModelPortfolio;
import com.wealth.goalsadvisory.entity.Recommendation;
import com.wealth.goalsadvisory.enums.ModelPortfolioStatus;
import com.wealth.goalsadvisory.enums.RecommendationStatus;
import com.wealth.goalsadvisory.exception.ResourceNotFoundException;
import com.wealth.goalsadvisory.repository.ModelPortfolioRepository;
import com.wealth.goalsadvisory.repository.RecommendationRepository;
import com.wealth.goalsadvisory.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final ModelPortfolioRepository modelPortfolioRepository;
    private final ModelMapper mapper;

    @Override
    public RecommendationResponse createRecommendation(RecommendationRequest request) {
        List<ModelPortfolio> matchingPortfolios = modelPortfolioRepository.findByRiskClassAndStatus(
                        request.getRiskClass(), ModelPortfolioStatus.ACTIVE);
        if (matchingPortfolios.isEmpty()) {
            throw new ResourceNotFoundException("No active model portfolio found for risk class: " + request.getRiskClass() + ". Please ask Admin to create one.");
        }
        ModelPortfolio modelPortfolio = matchingPortfolios.get(0);
        Recommendation recommendation = new Recommendation();
        recommendation.setClientId(request.getClientId());
        recommendation.setModelPortfolio(modelPortfolio);
        recommendation.setProposalJson(request.getProposalJson());
        if (request.getProposedDate() != null) {
            recommendation.setProposedDate(request.getProposedDate());
        } else {
            recommendation.setProposedDate(LocalDate.now());
        }
        if (request.getStatus() != null) {
            recommendation.setStatus(request.getStatus());
        } else {
            recommendation.setStatus(RecommendationStatus.DRAFT);
        }
        Recommendation saved = recommendationRepository.save(recommendation);
        return mapper.map(saved, RecommendationResponse.class);
    }
    @Override
    public List<RecommendationResponse> getAllRecommendations() {
        List<Recommendation> recommendations = recommendationRepository.findAll();
        List<RecommendationResponse> responseList = new ArrayList<>();
        for (Recommendation recommendation : recommendations) {
            responseList.add(mapper.map(recommendation, RecommendationResponse.class));
        }
        return responseList;
    }
    @Override
    public RecommendationResponse getRecommendationById(Long id) {
        Optional<Recommendation> optional = recommendationRepository.findById(id);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Recommendation not found with id: " + id);
        }
        return mapper.map(optional.get(), RecommendationResponse.class);
    }
    @Override
    public List<RecommendationResponse> getRecommendationsByClientId(Long clientId) {
        List<Recommendation> recommendations =
                recommendationRepository.findByClientId(clientId);
        List<RecommendationResponse> responseList = new ArrayList<>();
        for (Recommendation recommendation : recommendations) {
            responseList.add(mapper.map(recommendation, RecommendationResponse.class));
        }
        return responseList;
    }
    @Override
    public List<RecommendationResponse> getRecommendationsByClientIdAndStatus(
            Long clientId, RecommendationStatus status) {
        List<Recommendation> recommendations =
                recommendationRepository.findByClientIdAndStatus(clientId, status);
        List<RecommendationResponse> responseList = new ArrayList<>();
        for (Recommendation recommendation : recommendations) {
            responseList.add(mapper.map(recommendation, RecommendationResponse.class));
        }
        return responseList;
    }
    @Override
    public List<RecommendationResponse> getRecommendationsByModelId(Long modelId) {
        List<Recommendation> recommendations =
                recommendationRepository.findByModelPortfolio_ModelId(modelId);
        List<RecommendationResponse> responseList = new ArrayList<>();
        for (Recommendation recommendation : recommendations) {
            responseList.add(mapper.map(recommendation, RecommendationResponse.class));
        }
        return responseList;
    }
    @Override
    public RecommendationResponse updateRecommendationStatus(
            Long id, RecommendationStatus newStatus) {

        Optional<Recommendation> optional = recommendationRepository.findById(id);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Recommendation not found with id: " + id);
        }
        Recommendation recommendation = optional.get();
        validateStatusTransition(recommendation.getStatus(), newStatus);
        recommendation.setStatus(newStatus);

        return mapper.map(
                recommendationRepository.save(recommendation),
                RecommendationResponse.class);
    }

    @Override
    public RecommendationResponse updateRecommendation(Long id, RecommendationRequest request) {

        Optional<Recommendation> optional = recommendationRepository.findById(id);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Recommendation not found with id: " + id);
        }
        Recommendation recommendation = optional.get();
        if (recommendation.getStatus() != RecommendationStatus.DRAFT) {
            throw new IllegalArgumentException("Only DRAFT recommendations can be updated. " + "Current status: " + recommendation.getStatus());
        }
        List<ModelPortfolio> matchingPortfolios =
                modelPortfolioRepository.findByRiskClassAndStatus(request.getRiskClass(), ModelPortfolioStatus.ACTIVE);

        if (matchingPortfolios.isEmpty()) {
            throw new ResourceNotFoundException("No active model portfolio found for risk class: " + request.getRiskClass());
        }
        recommendation.setClientId(request.getClientId());
        recommendation.setModelPortfolio(matchingPortfolios.get(0));
        recommendation.setProposalJson(request.getProposalJson());

        if (request.getProposedDate() != null) {
            recommendation.setProposedDate(request.getProposedDate());
        }
        return mapper.map(recommendationRepository.save(recommendation), RecommendationResponse.class);
    }

    @Override
    public void deleteRecommendation(Long id) {
        Optional<Recommendation> optional = recommendationRepository.findById(id);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Recommendation not found with id: " + id);
        }

        Recommendation recommendation = optional.get();

        if (recommendation.getStatus() != RecommendationStatus.DRAFT) {
            throw new IllegalArgumentException("Only DRAFT recommendations can be deleted. " + "Current status: " + recommendation.getStatus());
        }

        recommendationRepository.delete(recommendation);
    }

    private void validateStatusTransition(
            RecommendationStatus current, RecommendationStatus next) {

        boolean valid = false;

        if (current == RecommendationStatus.DRAFT
                && next == RecommendationStatus.SUBMITTED) {
            valid = true;
        } else if (current == RecommendationStatus.SUBMITTED
                && next == RecommendationStatus.APPROVED) {
            valid = true;
        } else if (current == RecommendationStatus.SUBMITTED
                && next == RecommendationStatus.REJECTED) {
            valid = true;
        }

        if (!valid) {
            throw new IllegalArgumentException("Invalid status transition from " + current + " to " + next
                            + ". Allowed: DRAFT→SUBMITTED, SUBMITTED→APPROVED/REJECTED");
        }
    }
}