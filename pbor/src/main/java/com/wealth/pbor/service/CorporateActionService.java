package com.wealth.pbor.service;

import com.wealth.pbor.dto.request.CorporateActionRequest;
import com.wealth.pbor.dto.response.CorporateActionResponse;
import com.wealth.pbor.enums.CAType;

import java.time.LocalDate;
import java.util.List;

public interface CorporateActionService {

    CorporateActionResponse createCorporateAction(CorporateActionRequest requestDTO);

    CorporateActionResponse getCorporateActionById(Long caId);

    List<CorporateActionResponse> getAllCorporateActions();

    List<CorporateActionResponse> getCorporateActionsBySecurityId(Long securityId);

    List<CorporateActionResponse> getCorporateActionsByCaType(CAType caType);

    List<CorporateActionResponse> getCorporateActionsByRecordDateRange(LocalDate from, LocalDate to);

    CorporateActionResponse updateCorporateAction(Long caId, CorporateActionRequest requestDTO);

    void deleteCorporateAction(Long caId);
}