package com.wealthpro.productcatalog.dto.request;

import com.wealthpro.productcatalog.enums.AssetClass;
import com.wealthpro.productcatalog.enums.SecurityStatus;
import com.wealthpro.productcatalog.validation.ValidCurrencyCode;
import com.wealthpro.productcatalog.validation.ValidSymbol;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SecurityRequest {

    @NotBlank(message = "Symbol is required")
    @Size(max = 20, message = "Symbol must not exceed 20 characters")
    @ValidSymbol
    private String symbol;

    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @Size(max = 20, message = "Exchange must not exceed 20 characters")
    private String exchange;

    @Size(min = 12, max = 12, message = "ISIN must be exactly 12 characters")
    private String isin;

    @NotNull(message = "Asset class is required")
    private AssetClass assetClass;

    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency must not exceed 10 characters")
    @ValidCurrencyCode
    private String currency;

    @NotBlank(message = "Country is required")
    @Size(min=2,max = 50, message = "Country must not exceed 50 characters")
    private String country;

    @NotNull(message = "Status is required")
    private SecurityStatus status;

    /** Current market price (optional — null is fine for NAV-priced instruments). */
    @DecimalMin(value = "0.0001", message = "Price must be positive")
    @Digits(integer = 11, fraction = 4, message = "Price must have at most 11 integer digits and 4 decimal places")
    private BigDecimal currentPrice;
}