package com.wealthpro.productcatalog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductTermResponse {

    private Long termId;
    private Long securityId;
    private String securitySymbol;
    private String termJson;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}