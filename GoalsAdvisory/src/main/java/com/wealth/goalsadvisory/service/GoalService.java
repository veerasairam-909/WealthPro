package com.wealth.goalsadvisory.service;

import com.wealth.goalsadvisory.dto.request.GoalRequest;
import com.wealth.goalsadvisory.dto.response.GoalResponse;
import com.wealth.goalsadvisory.enums.GoalStatus;
import java.util.List;
public interface GoalService {
    GoalResponse createGoal(GoalRequest request);
    List<GoalResponse> getAllGoals();
    GoalResponse getGoalById(Long id);
    List<GoalResponse> getGoalsByClientId(Long clientId);
    List<GoalResponse> getGoalsByClientIdAndStatus(Long clientId, GoalStatus status);
    GoalResponse updateGoal(Long id, GoalRequest request);
    void deleteGoal(Long id);
}