package com.wealth.goalsadvisory.service.Impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealth.goalsadvisory.dto.request.ModelPortfolioRequest;
import com.wealth.goalsadvisory.dto.response.ModelPortfolioResponse;
import com.wealth.goalsadvisory.entity.ModelPortfolio;
import com.wealth.goalsadvisory.enums.ModelPortfolioStatus;
import com.wealth.goalsadvisory.enums.RiskClass;
import com.wealth.goalsadvisory.exception.ResourceNotFoundException;
import com.wealth.goalsadvisory.repository.ModelPortfolioRepository;
import com.wealth.goalsadvisory.service.ModelPortfolioService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModelPortfolioServiceImpl implements ModelPortfolioService {

    private final ModelPortfolioRepository modelPortfolioRepository;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    public ModelPortfolioResponse createModelPortfolio(ModelPortfolioRequest request) {
        if (modelPortfolioRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException(
                    "Model portfolio already exists with name: " + request.getName());
        }

        validateWeightsJson(request.getWeightsJson());

        ModelPortfolio portfolio = new ModelPortfolio();
        portfolio.setName(request.getName());
        portfolio.setRiskClass(request.getRiskClass());
        portfolio.setWeightsJson(request.getWeightsJson());

        if (request.getStatus() != null) {
            portfolio.setStatus(request.getStatus());
        } else {
            portfolio.setStatus(ModelPortfolioStatus.ACTIVE);
        }

        ModelPortfolio saved = modelPortfolioRepository.save(portfolio);
        return mapper.map(saved, ModelPortfolioResponse.class);
    }

    @Override
    public List<ModelPortfolioResponse> getAllModelPortfolios() {
        List<ModelPortfolio> portfolios = modelPortfolioRepository.findAll();
        List<ModelPortfolioResponse> responseList = new ArrayList<>();
        for (ModelPortfolio portfolio : portfolios) {
            responseList.add(mapper.map(portfolio, ModelPortfolioResponse.class));
        }
        return responseList;
    }

    @Override
    public ModelPortfolioResponse getModelPortfolioById(Long id) {
        Optional<ModelPortfolio> optionalPortfolio = modelPortfolioRepository.findById(id);
        if (optionalPortfolio.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Model portfolio not found with id: " + id);
        }
        return mapper.map(optionalPortfolio.get(), ModelPortfolioResponse.class);
    }

    @Override
    public List<ModelPortfolioResponse> getModelPortfoliosByRiskClass(RiskClass riskClass) {
        List<ModelPortfolio> portfolios = modelPortfolioRepository.findByRiskClass(riskClass);
        List<ModelPortfolioResponse> responseList = new ArrayList<>();
        for (ModelPortfolio portfolio : portfolios) {
            responseList.add(mapper.map(portfolio, ModelPortfolioResponse.class));
        }
        return responseList;
    }

    @Override
    public List<ModelPortfolioResponse> getModelPortfoliosByStatus(ModelPortfolioStatus status) {
        List<ModelPortfolio> portfolios = modelPortfolioRepository.findByStatus(status);
        List<ModelPortfolioResponse> responseList = new ArrayList<>();
        for (ModelPortfolio portfolio : portfolios) {
            responseList.add(mapper.map(portfolio, ModelPortfolioResponse.class));
        }
        return responseList;
    }

    @Override
    public ModelPortfolioResponse updateModelPortfolio(Long id, ModelPortfolioRequest request) {
        Optional<ModelPortfolio> optionalPortfolio = modelPortfolioRepository.findById(id);
        if (optionalPortfolio.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Model portfolio not found with id: " + id);
        }

        ModelPortfolio portfolio = optionalPortfolio.get();

        if (!portfolio.getName().equals(request.getName())
                && modelPortfolioRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException(
                    "Model portfolio already exists with name: " + request.getName());
        }

        validateWeightsJson(request.getWeightsJson());

        portfolio.setName(request.getName());
        portfolio.setRiskClass(request.getRiskClass());
        portfolio.setWeightsJson(request.getWeightsJson());

        if (request.getStatus() != null) {
            portfolio.setStatus(request.getStatus());
        }

        ModelPortfolio updated = modelPortfolioRepository.save(portfolio);
        return mapper.map(updated, ModelPortfolioResponse.class);
    }

    @Override
    public void deleteModelPortfolio(Long id) {
        Optional<ModelPortfolio> optionalPortfolio = modelPortfolioRepository.findById(id);
        if (optionalPortfolio.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Model portfolio not found with id: " + id);
        }
        modelPortfolioRepository.delete(optionalPortfolio.get());
    }

    /**
     * Validates that weightsJson is valid JSON and that all weight values sum to 100%.
     * Expected format: {"EQUITY": 60, "DEBT": 30, "GOLD": 10}
     */
    private void validateWeightsJson(String weightsJson) {
        if (weightsJson == null || weightsJson.isBlank()) {
            throw new IllegalArgumentException("Weights JSON is required");
        }
        try {
            Map<String, BigDecimal> weights = objectMapper.readValue(weightsJson,
                    new TypeReference<Map<String, BigDecimal>>() {});

            if (weights.isEmpty()) {
                throw new IllegalArgumentException("Weights JSON must contain at least one asset class");
            }

            BigDecimal total = weights.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (total.compareTo(BigDecimal.valueOf(100)) != 0) {
                throw new IllegalArgumentException(
                        "Weights must sum to 100%. Current total: " + total + "%");
            }

            boolean hasNegative = weights.values().stream()
                    .anyMatch(v -> v.compareTo(BigDecimal.ZERO) < 0);
            if (hasNegative) {
                throw new IllegalArgumentException("Weight values cannot be negative");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid weights JSON format: " + e.getMessage());
        }
    }
}