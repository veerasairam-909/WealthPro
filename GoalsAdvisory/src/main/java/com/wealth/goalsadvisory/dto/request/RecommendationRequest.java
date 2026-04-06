package com.wealth.goalsadvisory.dto.request;

import com.wealth.goalsadvisory.enums.RecommendationStatus;
import com.wealth.goalsadvisory.enums.RiskClass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RecommendationRequest {

    @NotNull(message = "{com.wealth.goalsadvisory.dto.request.recommendationrequest.name.blank}")
    private Long clientId;
    @NotNull(message = "{com.wealth.goalsadvisory.dto.request.modelrequest.riskclass.blank}")
    private RiskClass riskClass;
    @NotBlank(message = "{com.wealth.goalsadvisory.dto.request.recommendationrequest.proposaljson.blank}")
    private String proposalJson;
    @PastOrPresent(message = "{com.wealth.goalsadvisory.dto.request.recommendationrequest.date.error}")
    @NotNull(message="{com.wealth.goalsadvisory.dto.request.recommendationrequest.date.blank}")
    private LocalDate proposedDate;
    @NotNull(message = "{com.wealth.goalsadvisory.dto.request.recommendationrequest.status.blank}")
    private RecommendationStatus status;
}