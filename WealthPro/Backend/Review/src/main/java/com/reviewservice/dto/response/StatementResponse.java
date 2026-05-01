package com.reviewservice.dto.response;

import com.reviewservice.enums.PeriodType;
import com.reviewservice.enums.StatementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatementResponse {

    private Long statementId;
    private Long accountId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private PeriodType periodType;
    private LocalDate generatedDate;
    private String summaryJson;
    private StatementStatus status;
}
