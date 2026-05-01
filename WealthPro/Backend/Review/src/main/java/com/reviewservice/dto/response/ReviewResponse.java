package com.reviewservice.dto.response;

import com.reviewservice.enums.PeriodType;
import com.reviewservice.enums.ReviewStatus;
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
public class ReviewResponse {

    private Long reviewId;
    private Long accountId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private PeriodType periodType;
    private String highlightsJson;
    private String reviewedBy;
    private LocalDate reviewDate;
    private ReviewStatus status;
}
