package com.wealth.goalsadvisory.controller.impl;

import com.wealth.goalsadvisory.controller.GoalController;
import com.wealth.goalsadvisory.dto.request.GoalRequest;
import com.wealth.goalsadvisory.dto.response.GoalResponse;
import com.wealth.goalsadvisory.enums.GoalStatus;
import com.wealth.goalsadvisory.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalControllerImpl implements GoalController {

    private final GoalService goalService;

    @PostMapping
    @Override
    public ResponseEntity<GoalResponse> createGoal(
            @Valid @RequestBody GoalRequest request) {
        GoalResponse response = goalService.createGoal(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Override
    public ResponseEntity<List<GoalResponse>> getAllGoals() {

        return ResponseEntity.ok(goalService.getAllGoals());
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<GoalResponse> getGoalById(
            @PathVariable Long id) {
        return ResponseEntity.ok(goalService.getGoalById(id));
    }

    @GetMapping("/client/{clientId}")
    @Override
    public ResponseEntity<List<GoalResponse>> getGoalsByClientId(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(goalService.getGoalsByClientId(clientId));
    }

    @GetMapping("/client/{clientId}/status/{status}")
    @Override
    public ResponseEntity<List<GoalResponse>> getGoalsByClientIdAndStatus(
            @PathVariable Long clientId,
            @PathVariable GoalStatus status) {
        return ResponseEntity.ok(
                goalService.getGoalsByClientIdAndStatus(clientId, status));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<GoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody GoalRequest request) {
        return ResponseEntity.ok(goalService.updateGoal(id, request));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<String> deleteGoal(@PathVariable Long id) {
        goalService.deleteGoal(id);
        return ResponseEntity.ok("Goal deleted successfully with id: " + id);
    }
}