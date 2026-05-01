package com.wealth.goalsadvisory.controller.impl;

import com.wealth.goalsadvisory.controller.GoalController;
import com.wealth.goalsadvisory.dto.request.GoalRequest;
import com.wealth.goalsadvisory.dto.response.GoalResponse;
import com.wealth.goalsadvisory.enums.GoalStatus;
import com.wealth.goalsadvisory.security.AuthContext;
import com.wealth.goalsadvisory.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalControllerImpl implements GoalController {

    private final GoalService goalService;

    @PostMapping
    @Override
    public ResponseEntity<GoalResponse> createGoal(
            @Valid @RequestBody GoalRequest request,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient() && !ctx.ownsClient(request.getClientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Clients can only create goals for themselves");
        }
        GoalResponse response = goalService.createGoal(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Override
    public ResponseEntity<List<GoalResponse>> getAllGoals(
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Clients cannot list all goals; use /client/{yourClientId}");
        }
        return ResponseEntity.ok(goalService.getAllGoals());
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<GoalResponse> getGoalById(
            @PathVariable Long id,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        GoalResponse goal = goalService.getGoalById(id);
        if (ctx.isClient() && !ctx.ownsClient(goal.getClientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have access to this goal");
        }
        return ResponseEntity.ok(goal);
    }

    @GetMapping("/client/{clientId}")
    @Override
    public ResponseEntity<List<GoalResponse>> getGoalsByClientId(
            @PathVariable Long clientId,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient() && !ctx.ownsClient(clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only view your own goals");
        }
        return ResponseEntity.ok(goalService.getGoalsByClientId(clientId));
    }

    @GetMapping("/client/{clientId}/status/{status}")
    @Override
    public ResponseEntity<List<GoalResponse>> getGoalsByClientIdAndStatus(
            @PathVariable Long clientId,
            @PathVariable GoalStatus status,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient() && !ctx.ownsClient(clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only view your own goals");
        }
        return ResponseEntity.ok(
                goalService.getGoalsByClientIdAndStatus(clientId, status));
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<GoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody GoalRequest request,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        GoalResponse existing = goalService.getGoalById(id);
        if (ctx.isClient() && !ctx.ownsClient(existing.getClientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You cannot modify another client's goal");
        }
        return ResponseEntity.ok(goalService.updateGoal(id, request));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<String> deleteGoal(
            @PathVariable Long id,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        AuthContext ctx = new AuthContext(null, roles, authClientId);
        GoalResponse existing = goalService.getGoalById(id);
        if (ctx.isClient() && !ctx.ownsClient(existing.getClientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You cannot delete another client's goal");
        }
        goalService.deleteGoal(id);
        return ResponseEntity.ok("Goal deleted successfully with id: " + id);
    }
}
