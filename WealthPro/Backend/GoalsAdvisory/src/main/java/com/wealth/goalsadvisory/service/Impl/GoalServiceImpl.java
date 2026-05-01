package com.wealth.goalsadvisory.service.Impl;

import com.wealth.goalsadvisory.dto.request.GoalRequest;
import com.wealth.goalsadvisory.dto.response.GoalResponse;
import com.wealth.goalsadvisory.entity.Goal;
import com.wealth.goalsadvisory.enums.GoalStatus;
import com.wealth.goalsadvisory.exception.ResourceNotFoundException;
import com.wealth.goalsadvisory.feign.WealthproFeignClient;
import com.wealth.goalsadvisory.feign.dto.ClientDTO;
import com.wealth.goalsadvisory.repository.GoalRepository;
import com.wealth.goalsadvisory.service.GoalService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final ModelMapper mapper;
    private final WealthproFeignClient wealthproFeignClient;

    @Override
    public GoalResponse createGoal(GoalRequest request) {

        // ── Feign: Validate client exists in Wealthpro service ──────────────
        try {
            ClientDTO client = wealthproFeignClient.getClientById(request.getClientId());
            log.info("[FEIGN] Client validated from WEALTHPRO-SERVICE → id={}, name={}, segment={}",
                    client.getClientId(), client.getName(), client.getSegment());
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException(
                    "Client not found with ID: " + request.getClientId() +
                    " (validated via WEALTHPRO-SERVICE)");
        }
        // ────────────────────────────────────────────────────────────────────

        Goal goal = new Goal();
        goal.setClientId(request.getClientId());
        goal.setGoalType(request.getGoalType());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        goal.setPriority(request.getPriority());

        if (request.getStatus() != null) {
            goal.setStatus(request.getStatus());
        } else {
            goal.setStatus(GoalStatus.ACTIVE);
        }
        Goal saved = goalRepository.save(goal);
        return mapper.map(saved, GoalResponse.class);
    }

    @Override
    public List<GoalResponse> getAllGoals() {
        List<Goal> goals = goalRepository.findAll();
        List<GoalResponse> responseList = new ArrayList<>();
        for (Goal goal : goals) {
            responseList.add(mapper.map(goal, GoalResponse.class));
        }
        return responseList;
    }

    @Override
    public GoalResponse getGoalById(Long id) {
        Optional<Goal> optionalGoal = goalRepository.findById(id);
        if (optionalGoal.isEmpty()) {
            throw new ResourceNotFoundException("Goal not found with id: " + id);
        }
        return mapper.map(optionalGoal.get(), GoalResponse.class);
    }

    @Override
    public List<GoalResponse> getGoalsByClientId(Long clientId) {
        List<Goal> goals = goalRepository.findByClientId(clientId);
        List<GoalResponse> responseList = new ArrayList<>();
        for (Goal goal : goals) {
            responseList.add(mapper.map(goal, GoalResponse.class));
        }
        return responseList;
    }

    @Override
    public List<GoalResponse> getGoalsByClientIdAndStatus(Long clientId, GoalStatus status) {
        List<Goal> goals = goalRepository.findByClientIdAndStatus(clientId, status);
        List<GoalResponse> responseList = new ArrayList<>();
        for (Goal goal : goals) {
            responseList.add(mapper.map(goal, GoalResponse.class));
        }
        return responseList;
    }

    @Override
    public GoalResponse updateGoal(Long id, GoalRequest request) {
        Optional<Goal> optionalGoal = goalRepository.findById(id);
        if (optionalGoal.isEmpty()) {
            throw new ResourceNotFoundException("Goal not found with id: " + id);
        }

        Goal goal = optionalGoal.get();
        goal.setClientId(request.getClientId());
        goal.setGoalType(request.getGoalType());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        goal.setPriority(request.getPriority());

        if (request.getStatus() != null) {
            goal.setStatus(request.getStatus());
        }

        Goal updated = goalRepository.save(goal);
        return mapper.map(updated, GoalResponse.class);
    }

    @Override
    public void deleteGoal(Long id) {
        Optional<Goal> optionalGoal = goalRepository.findById(id);
        if (optionalGoal.isEmpty()) {
            throw new ResourceNotFoundException("Goal not found with id: " + id);
        }
        goalRepository.delete(optionalGoal.get());
    }
}
