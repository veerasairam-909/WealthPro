package com.wealthpro.dto.request;

import com.wealthpro.enums.ClientSegment;
import com.wealthpro.enums.ClientStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequestDTO {

    // @NotBlank = must not be null AND must not be empty/whitespace
    @NotBlank(message = "Client name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    @Pattern(regexp = "^[a-zA-Z ]+$",message = "Name must contain only letters and spaces")
    private String name;

    // @NotNull = must be provided
    // @Past = date must be in the past (valid DOB)
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    @NotBlank(message = "Contact info is required")
    private String contactInfo;

    @NotNull(message = "Segment is required (Retail / HNI / UHNI)")
    private ClientSegment segment;

    @NotNull(message = "Status is required (Active / Inactive)")
    private ClientStatus status;
}