package com.wealth.goalsadvisory.servicetestcases;

import com.wealth.goalsadvisory.dto.request.GoalRequest;
import com.wealth.goalsadvisory.dto.response.GoalResponse;
import com.wealth.goalsadvisory.entity.Goal;
import com.wealth.goalsadvisory.enums.GoalStatus;
import com.wealth.goalsadvisory.enums.GoalType;
import com.wealth.goalsadvisory.exception.ResourceNotFoundException;
import com.wealth.goalsadvisory.repository.GoalRepository;
import com.wealth.goalsadvisory.service.Impl.GoalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceUpdateDeleteTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private GoalServiceImpl goalService;

    private Goal sampleGoal;
    private GoalRequest updateRequest;

    @BeforeEach
    void setUp() {
        sampleGoal = new Goal();
        sampleGoal.setGoalId(1L);
        sampleGoal.setClientId(101L);
        sampleGoal.setGoalType(GoalType.RETIREMENT);
        sampleGoal.setTargetAmount(new BigDecimal("5000000.00"));
        sampleGoal.setTargetDate(LocalDate.of(2035, 12, 31));
        sampleGoal.setPriority(1);
        sampleGoal.setStatus(GoalStatus.ACTIVE);

        updateRequest = new GoalRequest();
        updateRequest.setClientId(101L);
        updateRequest.setGoalType(GoalType.WEALTH);
        updateRequest.setTargetAmount(new BigDecimal("8000000.00"));
        updateRequest.setTargetDate(LocalDate.of(2040, 6, 30));
        updateRequest.setPriority(2);
        updateRequest.setStatus(GoalStatus.IN_PROGRESS);
    }

    @Test
    void updateGoal_positive() {
        Goal updatedGoal = new Goal();
        updatedGoal.setGoalId(1L);
        updatedGoal.setClientId(101L);
        updatedGoal.setGoalType(GoalType.WEALTH);
        updatedGoal.setTargetAmount(new BigDecimal("8000000.00"));
        updatedGoal.setTargetDate(LocalDate.of(2040, 6, 30));
        updatedGoal.setPriority(2);
        updatedGoal.setStatus(GoalStatus.IN_PROGRESS);

        GoalResponse updatedResponse = new GoalResponse();
        updatedResponse.setGoalId(1L);
        updatedResponse.setGoalType(GoalType.WEALTH);
        updatedResponse.setTargetAmount(new BigDecimal("8000000.00"));
        updatedResponse.setStatus(GoalStatus.IN_PROGRESS);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(sampleGoal));
        when(goalRepository.save(any(Goal.class))).thenReturn(updatedGoal);
        when(mapper.map(any(Goal.class), eq(GoalResponse.class))).thenReturn(updatedResponse);

        GoalResponse response = goalService.updateGoal(1L, updateRequest);

        assertNotNull(response);
        assertEquals(GoalType.WEALTH, response.getGoalType());
        assertEquals(GoalStatus.IN_PROGRESS, response.getStatus());
    }

    @Test
    void updateGoal_negative() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> goalService.updateGoal(99L, updateRequest));

        verify(goalRepository, never()).save(any(Goal.class));
    }

    @Test
    void deleteGoal_positive() {
        when(goalRepository.findById(1L)).thenReturn(Optional.of(sampleGoal));
        doNothing().when(goalRepository).delete(sampleGoal);

        assertDoesNotThrow(() -> goalService.deleteGoal(1L));

        verify(goalRepository, times(1)).delete(sampleGoal);
    }

    @Test
    void deleteGoal_negative() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> goalService.deleteGoal(99L));

        verify(goalRepository, never()).delete(any(Goal.class));
    }
}