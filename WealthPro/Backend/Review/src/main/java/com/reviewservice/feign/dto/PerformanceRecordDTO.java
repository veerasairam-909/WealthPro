package com.reviewservice.feign.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PerformanceRecordDTO {
    private Long recordId;
    private Long accountId;
    private Long portfolioId;
    private String period;
    private BigDecimal returnPct;
    private LocalDate recordDate;
}
