package com.wealth.goalsadvisory.controller;

import com.wealth.goalsadvisory.dto.request.ModelPortfolioRequest;
import com.wealth.goalsadvisory.dto.response.ModelPortfolioResponse;
import com.wealth.goalsadvisory.enums.ModelPortfolioStatus;
import com.wealth.goalsadvisory.enums.RiskClass;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Model Portfolio Controller",
        description = "APIs for managing model portfolios used to recommend investment strategies based on client risk class. " +
                "Risk classes: CONSERVATIVE, BALANCED, AGGRESSIVE")
public interface ModelPortfolioController {

    @Operation(summary = "Create a new model portfolio",
            description = "Creates a new model portfolio. Name must be unique. Status defaults to ACTIVE if not provided. " +
                    "WeightsJSON example: {\"equity\": 40, \"bonds\": 40, \"derivatives\": 20}")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Model portfolio created successfully",
                    content = @Content(schema = @Schema(implementation = ModelPortfolioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Portfolio with same name already exists", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<ModelPortfolioResponse> createModelPortfolio(ModelPortfolioRequest request);

    @Operation(summary = "Get all model portfolios",
            description = "Retrieves all model portfolios regardless of status or risk class.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all model portfolios",
                    content = @Content(schema = @Schema(implementation = ModelPortfolioResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<List<ModelPortfolioResponse>> getAllModelPortfolios();

    @Operation(summary = "Get model portfolio by ID",
            description = "Retrieves a specific model portfolio by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Model portfolio retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ModelPortfolioResponse.class))),
            @ApiResponse(responseCode = "404", description = "Model portfolio not found with given ID", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<ModelPortfolioResponse> getModelPortfolioById(
            @Parameter(description = "ID of the model portfolio", required = true, example = "167")
            Long id);

    @Operation(summary = "Get model portfolios by risk class",
            description = "Retrieves all model portfolios matching the given risk class. Risk class values: CONSERVATIVE, BALANCED, AGGRESSIVE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Model portfolios retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ModelPortfolioResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<List<ModelPortfolioResponse>> getModelPortfoliosByRiskClass(
            @Parameter(description = "Risk class of the portfolio", required = true, example = "BALANCED")
            RiskClass riskClass);

    @Operation(summary = "Get model portfolios by status",
            description = "Retrieves all model portfolios with the given status. Status values: ACTIVE, INACTIVE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Model portfolios retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ModelPortfolioResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<List<ModelPortfolioResponse>> getModelPortfoliosByStatus(
            @Parameter(description = "Status of the portfolio", required = true, example = "ACTIVE")
            ModelPortfolioStatus status);

    @Operation(summary = "Update a model portfolio",
            description = "Updates an existing model portfolio by its ID. Name must be unique across all portfolios.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Model portfolio updated successfully",
                    content = @Content(schema = @Schema(implementation = ModelPortfolioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Duplicate name — portfolio with same name exists", content = @Content),
            @ApiResponse(responseCode = "404", description = "Model portfolio not found with given ID", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<ModelPortfolioResponse> updateModelPortfolio(
            @Parameter(description = "ID of the model portfolio to update", required = true, example = "167")
            Long id,
            ModelPortfolioRequest request);

    @Operation(summary = "Delete a model portfolio",
            description = "Deletes a model portfolio by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Model portfolio deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Model portfolio not found with given ID", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<String> deleteModelPortfolio(
            @Parameter(description = "ID of the model portfolio to delete", required = true, example = "167")
            Long id);
}