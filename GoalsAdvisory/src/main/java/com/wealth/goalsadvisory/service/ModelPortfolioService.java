package com.wealth.goalsadvisory.service;

import com.wealth.goalsadvisory.dto.request.ModelPortfolioRequest;
import com.wealth.goalsadvisory.dto.response.ModelPortfolioResponse;
import com.wealth.goalsadvisory.enums.ModelPortfolioStatus;
import com.wealth.goalsadvisory.enums.RiskClass;
import com.wealth.goalsadvisory.exception.ResourceNotFoundException;

import java.util.List;
public interface ModelPortfolioService {
    ModelPortfolioResponse createModelPortfolio(ModelPortfolioRequest request);
    List<ModelPortfolioResponse> getAllModelPortfolios();
    ModelPortfolioResponse getModelPortfolioById(Long id);
    List<ModelPortfolioResponse> getModelPortfoliosByRiskClass(RiskClass riskClass);
    List<ModelPortfolioResponse> getModelPortfoliosByStatus(ModelPortfolioStatus status);
    ModelPortfolioResponse updateModelPortfolio(Long id, ModelPortfolioRequest request);

    void deleteModelPortfolio(Long id) throws ResourceNotFoundException;
}