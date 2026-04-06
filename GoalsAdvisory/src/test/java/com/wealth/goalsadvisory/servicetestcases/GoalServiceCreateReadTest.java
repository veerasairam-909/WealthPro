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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceCreateReadTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private GoalServiceImpl goalService;

    private Goal sampleGoal;
    private GoalResponse sampleGoalResponse;
    private GoalRequest sampleRequest;

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

        sampleGoalResponse = new GoalResponse();
        sampleGoalResponse.setGoalId(1L);
        sampleGoalResponse.setClientId(101L);
        sampleGoalResponse.setGoalType(GoalType.RETIREMENT);
        sampleGoalResponse.setTargetAmount(new BigDecimal("5000000.00"));
        sampleGoalResponse.setTargetDate(LocalDate.of(2035, 12, 31));
        sampleGoalResponse.setPriority(1);
        sampleGoalResponse.setStatus(GoalStatus.ACTIVE);

        sampleRequest = new GoalRequest();
        sampleRequest.setClientId(101L);
        sampleRequest.setGoalType(GoalType.RETIREMENT);
        sampleRequest.setTargetAmount(new BigDecimal("5000000.00"));
        sampleRequest.setTargetDate(LocalDate.of(2035, 12, 31));
        sampleRequest.setPriority(1);
    }

    @Test
    void createGoal_positive() {
        when(goalRepository.save(any(Goal.class))).thenReturn(sampleGoal);
        when(mapper.map(any(Goal.class), eq(GoalResponse.class))).thenReturn(sampleGoalResponse);

        GoalResponse response = goalService.createGoal(sampleRequest);

        assertNotNull(response);
        assertEquals(101L, response.getClientId());
        assertEquals(GoalType.RETIREMENT, response.getGoalType());
        verify(goalRepository, times(1)).save(any(Goal.class));
    }

    @Test
    void createGoalWithDefaultStatus_WhenStatusNotGiven() {
        sampleRequest.setStatus(null);
        when(goalRepository.save(any(Goal.class))).thenReturn(sampleGoal);
        when(mapper.map(any(Goal.class), eq(GoalResponse.class))).thenReturn(sampleGoalResponse);

        GoalResponse response = goalService.createGoal(sampleRequest);

        assertEquals(GoalStatus.ACTIVE, response.getStatus());
    }

    @Test
    void createGoal_WhenStatusIsGiven() {
        sampleRequest.setStatus(GoalStatus.IN_PROGRESS);
        sampleGoal.setStatus(GoalStatus.IN_PROGRESS);
        sampleGoalResponse.setStatus(GoalStatus.IN_PROGRESS);
        when(goalRepository.save(any(Goal.class))).thenReturn(sampleGoal);
        when(mapper.map(any(Goal.class), eq(GoalResponse.class))).thenReturn(sampleGoalResponse);

        GoalResponse response = goalService.createGoal(sampleRequest);

        assertEquals(GoalStatus.IN_PROGRESS, response.getStatus());
    }

    @Test
    void getGoalById_positive() {
        when(goalRepository.findById(1L)).thenReturn(Optional.of(sampleGoal));
        when(mapper.map(any(Goal.class), eq(GoalResponse.class))).thenReturn(sampleGoalResponse);

        GoalResponse response = goalService.getGoalById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getGoalId());
    }

    @Test
    void getGoalById_negative() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> goalService.getGoalById(99L));
    }

    @Test
    void getAllGoals() {
        Goal goal2 = new Goal();
        goal2.setGoalId(2L);
        goal2.setClientId(102L);
        goal2.setGoalType(GoalType.EDUCATION);
        goal2.setStatus(GoalStatus.ACTIVE);

        GoalResponse response2 = new GoalResponse();
        response2.setGoalId(2L);
        response2.setGoalType(GoalType.EDUCATION);

        when(goalRepository.findAll()).thenReturn(Arrays.asList(sampleGoal, goal2));
        when(mapper.map(eq(sampleGoal), eq(GoalResponse.class))).thenReturn(sampleGoalResponse);
        when(mapper.map(eq(goal2), eq(GoalResponse.class))).thenReturn(response2);

        List<GoalResponse> responses = goalService.getAllGoals();

        assertEquals(2, responses.size());
        assertEquals(GoalType.RETIREMENT, responses.get(0).getGoalType());
        assertEquals(GoalType.EDUCATION, responses.get(1).getGoalType());
    }

    @Test
    void getGoalsByClientId() {
        when(goalRepository.findByClientId(101L)).thenReturn(List.of(sampleGoal));
        when(mapper.map(any(Goal.class), eq(GoalResponse.class))).thenReturn(sampleGoalResponse);

        List<GoalResponse> responses = goalService.getGoalsByClientId(101L);

        assertEquals(1, responses.size());
        assertEquals(101L, responses.get(0).getClientId());
    }

    @Test
    void getGoalsByClientIdAndStatus() {
        when(goalRepository.findByClientIdAndStatus(101L, GoalStatus.ACTIVE))
                .thenReturn(List.of(sampleGoal));
        when(mapper.map(any(Goal.class), eq(GoalResponse.class))).thenReturn(sampleGoalResponse);

        List<GoalResponse> responses =
                goalService.getGoalsByClientIdAndStatus(101L, GoalStatus.ACTIVE);

        assertEquals(1, responses.size());
        assertEquals(GoalStatus.ACTIVE, responses.get(0).getStatus());
    }
}