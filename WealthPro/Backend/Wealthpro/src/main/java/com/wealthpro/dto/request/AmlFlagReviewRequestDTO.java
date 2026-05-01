package com.wealthpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AmlFlagReviewRequestDTO {

    @NotBlank(message = "Status is required (REVIEWED, CLEARED, or ESCALATED)")
    private String status;

    private String notes;
}
