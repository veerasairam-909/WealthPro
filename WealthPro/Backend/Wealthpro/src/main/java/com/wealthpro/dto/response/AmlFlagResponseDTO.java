package com.wealthpro.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class AmlFlagResponseDTO {

    private Long amlFlagId;
    private Long clientId;
    private String flagType;
    private String description;
    private String status;
    private LocalDate flaggedDate;
    private String reviewedBy;
    private LocalDate reviewedDate;
    private String notes;
    private Long raisedByUserId;
}
