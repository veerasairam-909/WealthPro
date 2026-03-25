package com.wealth.goalsadvisory.controller;

import com.wealth.goalsadvisory.dto.request.GoalRequest;
import com.wealth.goalsadvisory.dto.response.GoalResponse;
import com.wealth.goalsadvisory.enums.GoalStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface GoalController {

    ResponseEntity<GoalResponse> createGoal(GoalRequest request);

    ResponseEntity<List<GoalResponse>> getAllGoals();

    ResponseEntity<GoalResponse> getGoalById(Long id);

    ResponseEntity<List<GoalResponse>> getGoalsByClientId(Long clientId);

    ResponseEntity<List<GoalResponse>> getGoalsByClientIdAndStatus(
            Long clientId, GoalStatus status);

    ResponseEntity<GoalResponse> updateGoal(Long id, GoalRequest request);

    ResponseEntity<String> deleteGoal(Long id);
}