package com.wealthpro.productcatalog.dto.response;

import com.wealthpro.productcatalog.enums.ResearchRating;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResearchNoteResponse {

    private Long noteId;
    private Long securityId;
    private String securitySymbol;
    private String title;
    private ResearchRating rating;
    private LocalDate publishedDate;
    private String contentUri;
}