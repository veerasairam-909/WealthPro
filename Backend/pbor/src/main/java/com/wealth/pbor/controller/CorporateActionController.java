package com.wealth.pbor.controller;

import com.wealth.pbor.dto.request.CorporateActionRequest;
import com.wealth.pbor.dto.response.CorporateActionResponse;
import com.wealth.pbor.enums.CAType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface CorporateActionController {

    ResponseEntity<CorporateActionResponse> createCorporateAction(CorporateActionRequest request);

    ResponseEntity<CorporateActionResponse> getCorporateActionById(Long caId);

    ResponseEntity<List<CorporateActionResponse>> getCorporateActionsBySecurityId(Long securityId);

    ResponseEntity<List<CorporateActionResponse>> getCorporateActionsByType(CAType caType);

    ResponseEntity<List<CorporateActionResponse>> getCorporateActionsBySecurityIdAndType(
            Long securityId, CAType caType);

    ResponseEntity<List<CorporateActionResponse>> getCorporateActionsByDateRange(
            LocalDate from, LocalDate to);

    ResponseEntity<List<CorporateActionResponse>> getUpcomingCorporateActions(Long securityId);

    ResponseEntity<CorporateActionResponse> updateCorporateAction(Long caId, CorporateActionRequest request);

    ResponseEntity<Void> deleteCorporateAction(Long caId);
}