package com.wealth.goalsadvisory.dto.response;

import com.wealth.goalsadvisory.enums.RecommendationStatus;
import com.wealth.goalsadvisory.enums.RiskClass;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RecommendationResponse {

    private Long recoId;
    private Long clientId;
    private Long modelId;
    private String modelName;
    private RiskClass riskClass;
    private String proposalJson;
    private LocalDate proposedDate;
    private RecommendationStatus status;
}