package com.wealth.pbor.dto.response;

import com.wealth.pbor.enums.CAType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CorporateActionResponse {

    private Long caId;
    private Long securityId;
    private CAType caType;
    private LocalDate recordDate;
    private LocalDate exDate;
    private LocalDate payDate;
    private String termsJson;
}
