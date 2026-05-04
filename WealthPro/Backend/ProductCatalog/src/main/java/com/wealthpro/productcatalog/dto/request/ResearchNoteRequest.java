package com.wealthpro.productcatalog.dto.request;

import com.wealthpro.productcatalog.enums.ResearchRating;
import com.wealthpro.productcatalog.validation.ValidContentUri;
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
public class ResearchNoteRequest {

    @NotNull(message = "Security ID is required")
    @Positive(message = "Security ID must be positive number")
    private Long securityId;

    @NotBlank(message = "Title is required")
    @Size(min=5,max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotNull(message = "Rating is required")
    private ResearchRating rating;

    @NotNull(message = "Published date is required")
    @PastOrPresent(message = "Published date cannot be future")
    private LocalDate publishedDate;

    @NotBlank(message = "Content URI is required")
    @Size(max = 500, message = "Content URI must not exceed 500 characters")
    @ValidContentUri
    private String contentUri;

    @Size(max = 100, message = "Analyst name must not exceed 100 characters")
    private String analyst;

    private String content;
}