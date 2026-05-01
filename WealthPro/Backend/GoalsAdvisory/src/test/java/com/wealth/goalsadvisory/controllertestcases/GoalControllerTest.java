package com.wealth.goalsadvisory.controllertestcases;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wealth.goalsadvisory.controller.impl.GoalControllerImpl;
import com.wealth.goalsadvisory.dto.request.GoalRequest;
import com.wealth.goalsadvisory.dto.response.GoalResponse;
import com.wealth.goalsadvisory.enums.GoalStatus;
import com.wealth.goalsadvisory.enums.GoalType;
import com.wealth.goalsadvisory.exception.ResourceNotFoundException;
import com.wealth.goalsadvisory.service.GoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GoalControllerImpl.class)
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoalService goalService;
    @Autowired
    private ObjectMapper objectMapper;
    private GoalRequest goalRequest;
    private GoalResponse goalResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        goalRequest = new GoalRequest();
        goalRequest.setClientId(101L);
        goalRequest.setGoalType(GoalType.RETIREMENT);
        goalRequest.setTargetAmount(new BigDecimal("5000000.00"));
        goalRequest.setTargetDate(LocalDate.of(2035, 12, 31));
        goalRequest.setPriority(1);
        goalRequest.setStatus(GoalStatus.ACTIVE);

        goalResponse = new GoalResponse();
        goalResponse.setGoalId(1L);
        goalResponse.setClientId(101L);
        goalResponse.setGoalType(GoalType.RETIREMENT);
        goalResponse.setTargetAmount(new BigDecimal("5000000.00"));
        goalResponse.setTargetDate(LocalDate.of(2035, 12, 31));
        goalResponse.setPriority(1);
        goalResponse.setStatus(GoalStatus.ACTIVE);
    }


    @Test
    void createGoal_positive() throws Exception {
        when(goalService.createGoal(any(GoalRequest.class)))
                .thenReturn(goalResponse);

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalRequest)))
                .andExpect(status().isCreated())//201
                .andExpect(jsonPath("$.goalId").value(1))
                .andExpect(jsonPath("$.clientId").value(101))
                .andExpect(jsonPath("$.goalType").value("RETIREMENT"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
//negative when client id is missing
    @Test
    void createGoal_negative() throws Exception {
        goalRequest.setClientId(null);

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getGoalById() throws Exception {
        when(goalService.getGoalById(1L)).thenReturn(goalResponse);

        mockMvc.perform(get("/api/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalId").value(1))
                .andExpect(jsonPath("$.goalType").value("RETIREMENT"));
    }
//when goal not found
    @Test
    void getGoalById_negative() throws Exception {
        when(goalService.getGoalById(99L)).thenThrow(new ResourceNotFoundException("Goal not found with id: 99"));

        mockMvc.perform(get("/api/goals/99")).andExpect(status().isNotFound());
    }

    @Test
    void deleteGoal() throws Exception {
        doNothing().when(goalService).deleteGoal(1L);

        mockMvc.perform(delete("/api/goals/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Goal deleted successfully with id: 1"));
    }
//when goal not found
    @Test
    void deleteGoal_negative() throws Exception {
        doThrow(new ResourceNotFoundException("Goal not found with id: 99"))
                .when(goalService).deleteGoal(99L);

        mockMvc.perform(delete("/api/goals/99"))
                .andExpect(status().isNotFound());
    }
    @Test
    void getAllGoals_success() throws Exception {
        when(goalService.getAllGoals()).thenReturn(java.util.List.of(goalResponse));

        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].goalId").value(1));
    }
    @Test
    void getGoalsByClientId_success() throws Exception {
        when(goalService.getGoalsByClientId(101L)).thenReturn(java.util.List.of(goalResponse));

        mockMvc.perform(get("/api/goals/client/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientId").value(101));
    }
    @Test
    void getGoalsByClientIdAndStatus_success() throws Exception {
        when(goalService.getGoalsByClientIdAndStatus(101L, GoalStatus.ACTIVE))
                .thenReturn(java.util.List.of(goalResponse));

        mockMvc.perform(get("/api/goals/client/101/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }
    @Test
    void updateGoal_success() throws Exception {
        when(goalService.updateGoal(eq(1L), any(GoalRequest.class)))
                .thenReturn(goalResponse);

        mockMvc.perform(put("/api/goals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalId").value(1));
    }
    @Test
    void updateGoal_invalidData() throws Exception {
        goalRequest.setTargetAmount(null); // Assuming this is mandatory

        mockMvc.perform(put("/api/goals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalRequest)))
                .andExpect(status().isBadRequest());
    }
}