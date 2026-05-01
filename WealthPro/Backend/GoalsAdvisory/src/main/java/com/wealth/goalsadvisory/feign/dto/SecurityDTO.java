package com.wealth.goalsadvisory.feign.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class SecurityDTO {
    private Long securityId;
    private String symbol;
    private String name;
    private String assetClass;
    private String status;
    private BigDecimal currentPrice;
    private String currency;
}
