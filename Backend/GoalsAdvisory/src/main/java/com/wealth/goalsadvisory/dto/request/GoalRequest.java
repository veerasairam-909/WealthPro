package com.wealth.goalsadvisory.dto.request;

import com.wealth.goalsadvisory.enums.GoalStatus;
import com.wealth.goalsadvisory.enums.GoalType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GoalRequest {

    @Positive(message = "{com.wealth.goalsadvisory.dto.request.goalrequest.clientid.error}")
    @NotNull(message = "{com.wealth.goalsadvisory.dto.request.goalrequest.clientid.blank}")
    private Long clientId;

    @NotNull(message = "{com.wealth.goalsadvisory.dto.request.goalrequest.goaltype.blank}")
    private GoalType goalType;

    @NotNull(message = "{com.wealth.goalsadvisory.dto.request.goalrequest.targetamount.blank}")
    @DecimalMin(value = "100", message = "{com.wealth.goalsadvisory.dto.request.goalrequest.targetdate.error}")
    private BigDecimal targetAmount;

    @NotNull(message = "{com.wealth.goalsadvisory.dto.request.goalrequest.targetdate.blank}")
    @Future(message = "Target date must be in the future")
    private LocalDate targetDate;

    @NotNull(message = "{com.wealth.goalsadvisory.dto.request.goalrequest.priority.blank}")
    @Min(value = 1, message = "{com.wealth.goalsadvisory.dto.request.goalrequest.priority.error}")
    private Integer priority;
    @NotNull(message = "{com.wealth.goalsadvisory.dto.request.goalrequest.status.blank}")
    private GoalStatus status;
}