package com.wealth.pbor.dto.response;

import com.wealth.pbor.enums.CAType;
import lombok.Data;

import java.time.LocalDate;
@Data
public class CorporateActionResponse {

    private Long caId;
    private Long securityId;
    private CAType caType;
    private LocalDate recordDate;
    private LocalDate exDate;
    private LocalDate payDate;
    private String termsJson;
}