package com.wealth.pbor.dto.request;

import com.wealth.pbor.enums.CAType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CorporateActionRequest {

    @NotNull(message = "{error.corporateaction.security.required}")
    @Positive(message = "{error.corporateaction.securityid.positive}")
    private Long securityId;

    @NotNull(message = "{error.corporateaction.catype.required}")
    private CAType caType;

    @NotNull(message = "{error.corporateaction.recorddate.required}")
    private LocalDate recordDate;

    @NotNull(message = "{error.corporateaction.exdate.required}")
    private LocalDate exDate;

    @NotNull(message = "{error.corporateaction.paydate.required}")
    private LocalDate payDate;

    @NotBlank(message = "{error.corporateaction.terms.required}")
    private String termsJson;
}