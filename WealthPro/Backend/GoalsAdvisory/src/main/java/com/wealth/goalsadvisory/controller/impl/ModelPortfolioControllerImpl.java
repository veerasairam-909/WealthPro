package com.wealth.goalsadvisory.controller.impl;

import com.wealth.goalsadvisory.controller.ModelPortfolioController;
import com.wealth.goalsadvisory.dto.request.ModelPortfolioRequest;
import com.wealth.goalsadvisory.dto.response.ModelPortfolioResponse;
import com.wealth.goalsadvisory.enums.ModelPortfolioStatus;
import com.wealth.goalsadvisory.enums.RiskClass;
import com.wealth.goalsadvisory.service.ModelPortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/model-portfolios")
@RequiredArgsConstructor
public class ModelPortfolioControllerImpl implements ModelPortfolioController {

    private final ModelPortfolioService modelPortfolioService;

    @PostMapping
    @Override
    public ResponseEntity<ModelPortfolioResponse> createModelPortfolio(
            @Valid @RequestBody ModelPortfolioRequest request) {
        ModelPortfolioResponse response =
                modelPortfolioService.createModelPortfolio(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Override
    public ResponseEntity<List<ModelPortfolioResponse>> getAllModelPortfolios() {
        return ResponseEntity.ok(modelPortfolioService.getAllModelPortfolios());
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<ModelPortfolioResponse> getModelPortfolioById(
            @PathVariable Long id) {
        return ResponseEntity.ok(modelPortfolioService.getModelPortfolioById(id));
    }

    @GetMapping("/risk-class/{riskClass}")
    @Override
    public ResponseEntity<List<ModelPortfolioResponse>> getModelPortfoliosByRiskClass(
            @PathVariable RiskClass riskClass) {
        return ResponseEntity.ok(
                modelPortfolioService.getModelPortfoliosByRiskClass(riskClass));
    }

    @GetMapping("/status/{status}")
    @Override
    public ResponseEntity<List<ModelPortfolioResponse>> getModelPortfoliosByStatus(
            @PathVariable ModelPortfolioStatus status) {
        return ResponseEntity.ok(
                modelPortfolioService.getModelPortfoliosByStatus(status));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ModelPortfolioResponse> updateModelPortfolio(
            @PathVariable Long id,
            @Valid @RequestBody ModelPortfolioRequest request) {
        return ResponseEntity.ok(
                modelPortfolioService.updateModelPortfolio(id, request));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<String> deleteModelPortfolio(@PathVariable Long id) {
        modelPortfolioService.deleteModelPortfolio(id);
        return ResponseEntity.ok(
                "Model portfolio deleted successfully with id: " + id);
    }
}