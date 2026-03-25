package com.wealth.goalsadvisory.controller;

import com.wealth.goalsadvisory.dto.request.ModelPortfolioRequest;
import com.wealth.goalsadvisory.dto.response.ModelPortfolioResponse;
import com.wealth.goalsadvisory.enums.ModelPortfolioStatus;
import com.wealth.goalsadvisory.enums.RiskClass;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ModelPortfolioController {

    ResponseEntity<ModelPortfolioResponse> createModelPortfolio(
            ModelPortfolioRequest request);

    ResponseEntity<List<ModelPortfolioResponse>> getAllModelPortfolios();

    ResponseEntity<ModelPortfolioResponse> getModelPortfolioById(Long id);

    ResponseEntity<List<ModelPortfolioResponse>> getModelPortfoliosByRiskClass(
            RiskClass riskClass);

    ResponseEntity<List<ModelPortfolioResponse>> getModelPortfoliosByStatus(
            ModelPortfolioStatus status);

    ResponseEntity<ModelPortfolioResponse> updateModelPortfolio(
            Long id, ModelPortfolioRequest request);

    ResponseEntity<String> deleteModelPortfolio(Long id);
}