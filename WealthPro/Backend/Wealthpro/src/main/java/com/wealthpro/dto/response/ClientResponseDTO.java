package com.wealthpro.dto.response;

import com.wealthpro.enums.ClientSegment;
import com.wealthpro.enums.ClientStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponseDTO {

    // Response includes the generated ID — request does not
    private Long clientId;
    private String username;   // login identifier (null if created by RM without login)
    private String name;
    private LocalDate dob;
    private String contactInfo;
    private ClientSegment segment;
    private ClientStatus status;

    // We do NOT include riskProfile or kycDocuments here
    // to keep the response clean and avoid circular serialization.
    // Use separate endpoints to fetch those.
}