package com.reviewservice.dto.request;

import com.reviewservice.enums.PeriodType;
import com.reviewservice.enums.StatementStatus;
import com.reviewservice.validation.ValidDateRange;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@ValidDateRange(
        startField = "periodStart",
        endField   = "periodEnd",
        message    = "{review.periodEnd.invalid}"
)
public class StatementRequest {

    @NotNull(message = "{statement.accountId.required}")
    private Long accountId;

    @NotNull(message = "{statement.periodStart.required}")
    private LocalDate periodStart;

    @NotNull(message = "{statement.periodEnd.required}")
    private LocalDate periodEnd;

    @NotNull(message = "{statement.periodType.required}")
    private PeriodType periodType;

    @NotNull(message = "{statement.generatedDate.required}")
    private LocalDate generatedDate;

    @NotBlank(message = "{statement.summaryJson.required}")
    @Size(max = 5000, message = "{statement.summaryJson.size}")
    private String summaryJson;

    @NotNull(message = "{statement.status.required}")
    private StatementStatus status;
}
