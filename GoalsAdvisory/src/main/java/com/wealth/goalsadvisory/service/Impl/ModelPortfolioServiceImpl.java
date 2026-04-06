package com.wealth.goalsadvisory.service.Impl;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModelPortfolioServiceImpl implements ModelPortfolioService {

    private final ModelPortfolioRepository modelPortfolioRepository;
    private final ModelMapper mapper;

    @Override
    public ModelPortfolioResponse createModelPortfolio(ModelPortfolioRequest request) {
        if (modelPortfolioRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException(
                    "Model portfolio already exists with name: " + request.getName());
        }

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
}