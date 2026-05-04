package com.wealthpro.productcatalog.dto.response;

import com.wealthpro.productcatalog.enums.AssetClass;
import com.wealthpro.productcatalog.enums.SecurityStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SecurityResponse {

    private long securityId;
    private String symbol;
    private String name;
    private String exchange;
    private String isin;
    private AssetClass assetClass;
    private String currency;
    private String country;
    private SecurityStatus status;
    /** Last known market price. Null for NAV-priced instruments (mutual funds). */
    private BigDecimal currentPrice;
}