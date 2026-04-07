package com.wealthpro.productcatalog.service;

import com.wealthpro.productcatalog.dto.request.SecurityRequest;
import com.wealthpro.productcatalog.dto.response.SecurityResponse;
import com.wealthpro.productcatalog.enums.AssetClass;
import com.wealthpro.productcatalog.enums.SecurityStatus;

import java.util.List;

public interface SecurityService {

    SecurityResponse createSecurity(SecurityRequest request);

    SecurityResponse getSecurityById(Long securityId);

    SecurityResponse getSecurityBySymbol(String symbol);

    List<SecurityResponse> getAllSecurities();

    List<SecurityResponse> getSecuritiesByAssetClass(AssetClass assetClass);

    List<SecurityResponse> getSecuritiesByStatus(SecurityStatus status);

    List<SecurityResponse> getSecuritiesByCountry(String country);

    List<SecurityResponse> getSecuritiesByAssetClassAndStatus(AssetClass assetClass, SecurityStatus status);

    SecurityResponse updateSecurity(Long securityId, SecurityRequest request);

    void deleteSecurity(Long securityId);
}