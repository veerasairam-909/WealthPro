package com.reviewservice.dto.request;

import com.reviewservice.enums.PeriodType;
import com.reviewservice.enums.ReviewStatus;
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
public class ReviewRequest {

    @NotNull(message = "{review.accountId.required}")
    private Long accountId;

    @NotNull(message = "{review.periodStart.required}")
    private LocalDate periodStart;

    @NotNull(message = "{review.periodEnd.required}")
    private LocalDate periodEnd;

    @NotNull(message = "{review.periodType.required}")
    private PeriodType periodType;

    @NotBlank(message = "{review.highlightsJson.required}")
    @Size(max = 5000, message = "{review.highlightsJson.size}")
    private String highlightsJson;

    @NotBlank(message = "{review.reviewedBy.required}")
    @Size(max = 150, message = "{review.reviewedBy.size}")
    private String reviewedBy;

    @NotNull(message = "{review.reviewDate.required}")
    private LocalDate reviewDate;

    @NotNull(message = "{review.status.required}")
    private ReviewStatus status;
}
