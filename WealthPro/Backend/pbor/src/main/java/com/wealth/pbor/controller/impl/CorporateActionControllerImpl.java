package com.wealth.pbor.controller.impl;

import com.wealth.pbor.dto.request.CorporateActionRequest;
import com.wealth.pbor.dto.response.CorporateActionResponse;
import com.wealth.pbor.enums.CAType;
import com.wealth.pbor.service.CorporateActionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/corporate-actions")
@RequiredArgsConstructor
public class CorporateActionControllerImpl {

    private final CorporateActionService corporateActionService;

    @PostMapping
    public ResponseEntity<CorporateActionResponse> createCorporateAction(@Valid @RequestBody CorporateActionRequest request) {
        CorporateActionResponse response = corporateActionService.createCorporateAction(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{caId}")
    public ResponseEntity<CorporateActionResponse> getCorporateActionById(@PathVariable Long caId) {
        CorporateActionResponse response = corporateActionService.getCorporateActionById(caId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CorporateActionResponse>> getAllCorporateActions() {
        List<CorporateActionResponse> responseList = corporateActionService.getAllCorporateActions();
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/security/{securityId}")
    public ResponseEntity<List<CorporateActionResponse>> getCorporateActionsBySecurityId(@PathVariable Long securityId) {
        List<CorporateActionResponse> responseList = corporateActionService.getCorporateActionsBySecurityId(securityId);
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/ca-type/{caType}")
    public ResponseEntity<List<CorporateActionResponse>> getCorporateActionsByCaType(@PathVariable CAType caType) {
        List<CorporateActionResponse> responseList = corporateActionService.getCorporateActionsByCaType(caType);
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<CorporateActionResponse>> getCorporateActionsByRecordDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<CorporateActionResponse> responseList = corporateActionService.getCorporateActionsByRecordDateRange(from, to);
        return ResponseEntity.ok(responseList);
    }

    @PutMapping("/{caId}")
    public ResponseEntity<CorporateActionResponse> updateCorporateAction(@PathVariable Long caId,
                                                                         @Valid @RequestBody CorporateActionRequest request) {
        CorporateActionResponse response = corporateActionService.updateCorporateAction(caId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{caId}")
    public ResponseEntity<String> deleteCorporateAction(@PathVariable Long caId) {
        corporateActionService.deleteCorporateAction(caId);
        return ResponseEntity.ok("Corporate action deleted successfully.");
    }
}