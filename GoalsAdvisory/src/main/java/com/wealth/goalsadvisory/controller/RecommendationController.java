package com.wealth.goalsadvisory.controller;

import com.wealth.goalsadvisory.dto.request.RecommendationRequest;
import com.wealth.goalsadvisory.dto.response.RecommendationResponse;
import com.wealth.goalsadvisory.enums.RecommendationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Recommendation Controller",
        description = "APIs for managing portfolio recommendations for clients. " +
                "RM creates a recommendation by passing clientId, riskClass and proposalJson. " +
                "System auto-picks the matching active ModelPortfolio based on riskClass. " +
                "Status lifecycle: DRAFT → SUBMITTED → APPROVED / REJECTED")
public interface RecommendationController {

    @Operation(summary = "Create a new recommendation",
            description = "RM creates a recommendation for a client. System automatically finds the matching ACTIVE ModelPortfolio based on riskClass. " +
                    "Status defaults to DRAFT if not provided. ProposalJSON should contain RM notes explaining why this portfolio suits the client.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Recommendation created successfully",
                    content = @Content(schema = @Schema(implementation = RecommendationResponse.class))),
            @ApiResponse(responseCode = "404", description = "No active model portfolio found for the given risk class", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<RecommendationResponse> createRecommendation(RecommendationRequest request);

    @Operation(summary = "Get all recommendations",
            description = "Retrieves all recommendations across all clients.")
    ResponseEntity<List<RecommendationResponse>> getAllRecommendations();

    @Operation(summary = "Get recommendation by ID",
            description = "Retrieves a specific recommendation by its ID.")
    ResponseEntity<RecommendationResponse> getRecommendationById(
            @Parameter(description = "ID of the recommendation", required = true, example = "1") Long id);

    @Operation(summary = "Get recommendations by client ID",
            description = "Retrieves all recommendations for a specific client.")
    ResponseEntity<List<RecommendationResponse>> getRecommendationsByClientId(
            @Parameter(description = "ID of the client", required = true, example = "101") Long clientId);

    @Operation(summary = "Get recommendations by client ID and status",
            description = "Retrieves recommendations for a client filtered by status. Status values: DRAFT, SUBMITTED, APPROVED, REJECTED")
    ResponseEntity<List<RecommendationResponse>> getRecommendationsByClientIdAndStatus(
            @Parameter(description = "ID of the client", required = true, example = "101") Long clientId,
            @Parameter(description = "Status of the recommendation", required = true, example = "DRAFT") RecommendationStatus status);

    @Operation(summary = "Get recommendations by model portfolio ID",
            description = "Retrieves all recommendations that use a specific model portfolio.")
    ResponseEntity<List<RecommendationResponse>> getRecommendationsByModelId(
            @Parameter(description = "ID of the model portfolio", required = true, example = "167") Long modelId);

    @Operation(summary = "Update recommendation status",
            description = "Updates only the status of a recommendation. Lifecycle rules: " +
                    "DRAFT → SUBMITTED (RM submits to client), SUBMITTED → APPROVED (client approves), SUBMITTED → REJECTED (client rejects). " +
                    "Pass status as raw JSON string in body. Example body: \"SUBMITTED\"")
    ResponseEntity<RecommendationResponse> updateRecommendationStatus(
            @Parameter(description = "ID of the recommendation", required = true, example = "1") Long id,
            @Parameter(description = "New status value", required = true, example = "SUBMITTED") RecommendationStatus status);

    @Operation(summary = "Update a recommendation",
            description = "Fully updates a recommendation. Only DRAFT recommendations can be updated. System re-picks portfolio based on new riskClass.")
    ResponseEntity<RecommendationResponse> updateRecommendation(
            @Parameter(description = "ID of the recommendation to update", required = true, example = "1") Long id,
            RecommendationRequest request);

    @Operation(summary = "Delete a recommendation",
            description = "Deletes a recommendation by its ID. Only DRAFT recommendations can be deleted.")
    ResponseEntity<String> deleteRecommendation(
            @Parameter(description = "ID of the recommendation to delete", required = true, example = "1") Long id);
}