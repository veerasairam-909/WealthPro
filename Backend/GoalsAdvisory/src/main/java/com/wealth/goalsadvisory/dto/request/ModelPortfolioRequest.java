package com.wealth.goalsadvisory.dto.request;

import com.wealth.goalsadvisory.enums.ModelPortfolioStatus;
import com.wealth.goalsadvisory.enums.RiskClass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ModelPortfolioRequest {

    @NotBlank(message = "{com.wealth.goalsadvisory.dto.request.modelrequest.name.blank}")
    @Pattern(regexp = "^[\\p{L}'\\s-]+$",message= "{com.wealth.goalsadvisory.dto.request.modelrequest.name.error}")
    private String name;

    @NotNull(message = "{com.wealth.goalsadvisory.dto.request.modelrequest.riskclass.blank}")
    private RiskClass riskClass;

    @NotBlank(message = "{com.wealth.goalsadvisory.dto.request.modelrequest.weightjson.blank}")
    private String weightsJson;

    @NotNull(message = "{com.wealth.goalsadvisory.dto.request.modelrequest.status.blank}")
    private ModelPortfolioStatus status;
}