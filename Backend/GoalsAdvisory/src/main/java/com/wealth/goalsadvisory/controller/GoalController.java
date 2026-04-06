package com.wealth.goalsadvisory.controller;

import com.wealth.goalsadvisory.dto.request.GoalRequest;
import com.wealth.goalsadvisory.dto.response.GoalResponse;
import com.wealth.goalsadvisory.enums.GoalStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Goal Controller",
        description = "APIs for managing client financial goals such as Retirement, Education, Wealth and Custom goals")
public interface GoalController {

    @Operation(summary = "Create a new goal",
            description = "Creates a new financial goal for a client. Status defaults to ACTIVE if not provided.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Goal created successfully",
                    content = @Content(schema = @Schema(implementation = GoalResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<GoalResponse> createGoal(GoalRequest request);

    @Operation(summary = "Get all goals",
            description = "Retrieves all financial goals across all clients.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all goals retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GoalResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<List<GoalResponse>> getAllGoals();

    @Operation(summary = "Get goal by ID",
            description = "Retrieves a specific financial goal by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Goal retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GoalResponse.class))),
            @ApiResponse(responseCode = "404", description = "Goal not found with given ID", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<GoalResponse> getGoalById(
            @Parameter(description = "ID of the goal to retrieve", required = true, example = "1")
            Long id);

    @Operation(summary = "Get goals by client ID",
            description = "Retrieves all financial goals belonging to a specific client.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Goals retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GoalResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<List<GoalResponse>> getGoalsByClientId(
            @Parameter(description = "ID of the client", required = true, example = "101")
            Long clientId);

    @Operation(summary = "Get goals by client ID and status",
            description = "Retrieves goals for a specific client filtered by status. Status values: ACTIVE, IN_PROGRESS, ACHIEVED, CANCELLED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered goals retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GoalResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<List<GoalResponse>> getGoalsByClientIdAndStatus(
            @Parameter(description = "ID of the client", required = true, example = "101")
            Long clientId,
            @Parameter(description = "Status of the goal", required = true, example = "ACTIVE")
            GoalStatus status);

    @Operation(summary = "Update a goal",
            description = "Updates an existing financial goal by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Goal updated successfully",
                    content = @Content(schema = @Schema(implementation = GoalResponse.class))),
            @ApiResponse(responseCode = "404", description = "Goal not found with given ID", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<GoalResponse> updateGoal(
            @Parameter(description = "ID of the goal to update", required = true, example = "1")
            Long id,
            GoalRequest request);

    @Operation(summary = "Delete a goal",
            description = "Deletes a financial goal by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Goal deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Goal not found with given ID", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    ResponseEntity<String> deleteGoal(
            @Parameter(description = "ID of the goal to delete", required = true, example = "1")
            Long id);
}