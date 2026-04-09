package com.wealthpro.dto.response;

import com.wealthpro.enums.RiskClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RiskProfileResponseDTO {

    private Long riskId;
    private Long clientId;          // just the ID, not the full Client object
    private String questionnaireJSON;
    private BigDecimal riskScore;
    private RiskClass riskClass;    // auto-calculated by service, returned in response
    private LocalDate assessedDate;
}