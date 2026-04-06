package com.wealthpro.productcatalog.controller;

import com.wealthpro.productcatalog.controller.SecurityController;
import com.wealthpro.productcatalog.dto.request.SecurityRequest;
import com.wealthpro.productcatalog.dto.response.SecurityResponse;
import com.wealthpro.productcatalog.enums.AssetClass;
import com.wealthpro.productcatalog.enums.SecurityStatus;
import com.wealthpro.productcatalog.service.SecurityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/securities")
public class SecurityControllerImpl implements SecurityController {

    private final SecurityService securityService;

    public SecurityControllerImpl(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    @PostMapping
    public ResponseEntity<SecurityResponse> createSecurity(
            @Valid @RequestBody SecurityRequest request) {
        SecurityResponse response = securityService.createSecurity(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/{securityId}")
    public ResponseEntity<SecurityResponse> getSecurityById(
            @PathVariable Long securityId) {
        SecurityResponse response = securityService.getSecurityById(securityId);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<SecurityResponse> getSecurityBySymbol(
            @PathVariable String symbol) {
        SecurityResponse response = securityService.getSecurityBySymbol(symbol);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<SecurityResponse>> getAllSecurities() {
        List<SecurityResponse> responses = securityService.getAllSecurities();
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/asset-class/{assetClass}")
    public ResponseEntity<List<SecurityResponse>> getSecuritiesByAssetClass(
            @PathVariable AssetClass assetClass) {
        List<SecurityResponse> responses = securityService.getSecuritiesByAssetClass(assetClass);
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SecurityResponse>> getSecuritiesByStatus(
            @PathVariable SecurityStatus status) {
        List<SecurityResponse> responses = securityService.getSecuritiesByStatus(status);
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/country/{country}")
    public ResponseEntity<List<SecurityResponse>> getSecuritiesByCountry(
            @PathVariable String country) {
        List<SecurityResponse> responses = securityService.getSecuritiesByCountry(country);
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/filter")
    public ResponseEntity<List<SecurityResponse>> getSecuritiesByAssetClassAndStatus(
            @RequestParam AssetClass assetClass,
            @RequestParam SecurityStatus status) {
        List<SecurityResponse> responses = securityService.getSecuritiesByAssetClassAndStatus(assetClass, status);
        return ResponseEntity.ok(responses);
    }

    @Override
    @PutMapping("/{securityId}")
    public ResponseEntity<SecurityResponse> updateSecurity(
            @PathVariable Long securityId,
            @Valid @RequestBody SecurityRequest request) {
        SecurityResponse response = securityService.updateSecurity(securityId, request);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{securityId}")
    public ResponseEntity deleteSecurity(@PathVariable Long securityId) {
        securityService.deleteSecurity(securityId);
        return ResponseEntity.ok("security deleted successfully");
    }
}