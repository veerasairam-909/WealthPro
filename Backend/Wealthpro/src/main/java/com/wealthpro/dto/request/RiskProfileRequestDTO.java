package com.wealthpro.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RiskProfileRequestDTO {

    // User sends answers like(example data):
    // {
    //   "answers": {
    //     "q1": "A",
    //     "q2": "C",
    //     "q3": "B",
    //     "q4": "D",
    //     "q5": "A"
    //   },
    //   "assessedDate": "2026-03-10"
    // }

    @NotNull(message = "Answers are required")
    @Size(min = 5, max = 5, message = "Exactly 5 answers required (q1 to q5)")
    private Map<String, String> answers;

    @NotNull(message = "Assessment date is required")
    private LocalDate assessedDate;

    // riskScore is NOT accepted from user
    // Service auto-calculates it from answers
    // riskClass is also NOT accepted — derived from score
}