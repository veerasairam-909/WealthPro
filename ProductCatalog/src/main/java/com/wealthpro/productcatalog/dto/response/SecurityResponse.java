package com.wealthpro.productcatalog.dto.response;

import com.wealthpro.productcatalog.enums.AssetClass;
import com.wealthpro.productcatalog.enums.SecurityStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SecurityResponse {

    private long securityId;
    private String symbol;
    private AssetClass assetClass;
    private String currency;
    private String country;
    private SecurityStatus status;
}