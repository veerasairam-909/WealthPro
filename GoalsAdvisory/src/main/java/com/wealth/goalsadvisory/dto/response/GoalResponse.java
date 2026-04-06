package com.wealth.goalsadvisory.dto.response;

import com.wealth.goalsadvisory.enums.GoalStatus;
import com.wealth.goalsadvisory.enums.GoalType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GoalResponse {

    private Long goalId;
    private Long clientId;
    private GoalType goalType;
    private BigDecimal targetAmount;
    private LocalDate targetDate;
    private Integer priority;
    private GoalStatus status;
}