package com.wealth.goalsadvisory.dto.response;

import com.wealth.goalsadvisory.enums.ModelPortfolioStatus;
import com.wealth.goalsadvisory.enums.RiskClass;
import lombok.Data;

@Data
public class ModelPortfolioResponse {

    private Long modelId;
    private String name;
    private RiskClass riskClass;
    private String weightsJson;
    private ModelPortfolioStatus status;
}