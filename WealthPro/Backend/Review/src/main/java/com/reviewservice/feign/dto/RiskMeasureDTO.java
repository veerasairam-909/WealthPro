package com.reviewservice.feign.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class RiskMeasureDTO {
    private Long measureId;
    private Long accountId;
    private String measureType;
    private BigDecimal value;
    private LocalDate calculatedDate;
}
