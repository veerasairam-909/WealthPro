package com.wealth.pbor.dto.request;

import com.wealth.pbor.enums.CAType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CorporateActionRequest {

    @NotNull(message = "{error.corporateaction.security.required}")
    @Positive(message = "{error.corporateaction.securityid.positive}")
    private Long securityId;

    @NotNull(message = "{error.corporateaction.account.required}")
    @Positive(message = "{error.corporateaction.accountid.positive}")
    private Long accountId;

    @NotNull(message = "{error.corporateaction.catype.required}")
    private CAType caType;

    @Positive(message = "{error.corporateaction.amount.positive}")
    private BigDecimal amount;

    @Size(min = 3, max = 3, message = "{error.corporateaction.currency.size}")
    private String currency;

    @Positive(message = "{error.corporateaction.bonusratio.positive}")
    private BigDecimal bonusRatio;

    @Positive(message = "{error.corporateaction.splitratio.positive}")
    private BigDecimal splitRatio;

    @NotNull(message = "{error.corporateaction.recorddate.required}")
    private LocalDate recordDate;

    @NotNull(message = "{error.corporateaction.exdate.required}")
    private LocalDate exDate;

    @NotNull(message = "{error.corporateaction.paydate.required}")
    private LocalDate payDate;

    @NotBlank(message = "{error.corporateaction.terms.required}")
    private String termsJson;
}
