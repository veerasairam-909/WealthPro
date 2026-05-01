package com.wealthpro.productcatalog.controller;

import com.wealthpro.productcatalog.dto.request.SecurityRequest;
import com.wealthpro.productcatalog.dto.response.SecurityResponse;
import com.wealthpro.productcatalog.enums.AssetClass;
import com.wealthpro.productcatalog.enums.SecurityStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface SecurityController {

    @PostMapping
    ResponseEntity<SecurityResponse> createSecurity(@Valid @RequestBody SecurityRequest request);

    @GetMapping("/{securityId}")
    ResponseEntity<SecurityResponse> getSecurityById(@PathVariable Long securityId);

    @GetMapping("/symbol/{symbol}")
    ResponseEntity<SecurityResponse> getSecurityBySymbol(@PathVariable String symbol);

    @GetMapping
    ResponseEntity<List<SecurityResponse>> getAllSecurities();

    @GetMapping("/asset-class/{assetClass}")
    ResponseEntity<List<SecurityResponse>> getSecuritiesByAssetClass(@PathVariable AssetClass assetClass);

    @GetMapping("/status/{status}")
    ResponseEntity<List<SecurityResponse>> getSecuritiesByStatus(@PathVariable SecurityStatus status);

    @GetMapping("/country/{country}")
    ResponseEntity<List<SecurityResponse>> getSecuritiesByCountry(@PathVariable String country);

    @GetMapping("/filter")
    ResponseEntity<List<SecurityResponse>> getSecuritiesByAssetClassAndStatus(
            @RequestParam AssetClass assetClass,
            @RequestParam SecurityStatus status);

    @PutMapping("/{securityId}")
    ResponseEntity<SecurityResponse> updateSecurity(
            @PathVariable Long securityId,
            @Valid @RequestBody SecurityRequest request);

    @DeleteMapping("/{securityId}")
    ResponseEntity deleteSecurity(@PathVariable Long securityId);
}