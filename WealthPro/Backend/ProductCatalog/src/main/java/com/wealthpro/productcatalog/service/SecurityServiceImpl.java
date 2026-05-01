package com.wealthpro.productcatalog.service;

import com.wealthpro.productcatalog.dto.request.SecurityRequest;
import com.wealthpro.productcatalog.dto.response.SecurityResponse;
import com.wealthpro.productcatalog.entity.Security;
import com.wealthpro.productcatalog.enums.AssetClass;
import com.wealthpro.productcatalog.enums.SecurityStatus;
import com.wealthpro.productcatalog.exception.DuplicateResourceException;
import com.wealthpro.productcatalog.exception.ResourceNotFoundException;
import com.wealthpro.productcatalog.repository.SecurityRepository;
import com.wealthpro.productcatalog.service.SecurityService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SecurityServiceImpl implements SecurityService {

    private final SecurityRepository securityRepository;
    private final ModelMapper modelMapper;

    public SecurityServiceImpl(SecurityRepository securityRepository, ModelMapper modelMapper) {
        this.securityRepository = securityRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public SecurityResponse createSecurity(SecurityRequest request) {
        if (securityRepository.existsBySymbolIgnoreCase(request.getSymbol())) {
            throw new DuplicateResourceException(
                    "Security already exists with symbol: " + request.getSymbol());
        }
        Security security = modelMapper.map(request, Security.class);
        Security saved = securityRepository.save(security);
        return modelMapper.map(saved, SecurityResponse.class);
    }

    @Override
    public SecurityResponse getSecurityById(Long securityId) {
        Security security = securityRepository.findById(securityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Security not found with id: " + securityId));
        return modelMapper.map(security, SecurityResponse.class);
    }

    @Override
    public SecurityResponse getSecurityBySymbol(String symbol) {
        Security security = securityRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Security not found with symbol: " + symbol));
        return modelMapper.map(security, SecurityResponse.class);
    }

    @Override
    public List<SecurityResponse> getAllSecurities() {
        List<Security> securities = securityRepository.findAll();
        List<SecurityResponse> responses = new ArrayList<>();
        for (Security security : securities) {
            responses.add(modelMapper.map(security, SecurityResponse.class));
        }
        return responses;
    }

    @Override
    public List<SecurityResponse> getSecuritiesByAssetClass(AssetClass assetClass) {
        List<Security> securities = securityRepository.findByAssetClass(assetClass);
        List<SecurityResponse> responses = new ArrayList<>();
        for (Security security : securities) {
            responses.add(modelMapper.map(security, SecurityResponse.class));
        }
        return responses;
    }

    @Override
    public List<SecurityResponse> getSecuritiesByStatus(SecurityStatus status) {
        List<Security> securities = securityRepository.findByStatus(status);
        List<SecurityResponse> responses = new ArrayList<>();
        for (Security security : securities) {
            responses.add(modelMapper.map(security, SecurityResponse.class));
        }
        return responses;
    }

    @Override
    public List<SecurityResponse> getSecuritiesByCountry(String country) {
        List<Security> securities = securityRepository.findByCountry(country);
        List<SecurityResponse> responses = new ArrayList<>();
        for (Security security : securities) {
            responses.add(modelMapper.map(security, SecurityResponse.class));
        }
        return responses;
    }

    @Override
    public List<SecurityResponse> getSecuritiesByAssetClassAndStatus(AssetClass assetClass, SecurityStatus status) {
        List<Security> securities = securityRepository.findByAssetClassAndStatus(assetClass, status);
        List<SecurityResponse> responses = new ArrayList<>();
        for (Security security : securities) {
            responses.add(modelMapper.map(security, SecurityResponse.class));
        }
        return responses;
    }

    @Override
    public SecurityResponse updateSecurity(Long securityId, SecurityRequest request) {
        Security existing = securityRepository.findById(securityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Security not found with id: " + securityId));

        if (!existing.getSymbol().equalsIgnoreCase(request.getSymbol())
                && securityRepository.existsBySymbolIgnoreCase(request.getSymbol())) {
            throw new DuplicateResourceException(
                    "Another security already exists with symbol: " + request.getSymbol());
        }

        existing.setSymbol(request.getSymbol());
        existing.setAssetClass(request.getAssetClass());
        existing.setCurrency(request.getCurrency());
        existing.setCountry(request.getCountry());
        existing.setStatus(request.getStatus());
        existing.setCurrentPrice(request.getCurrentPrice());

        Security updated = securityRepository.save(existing);
        return modelMapper.map(updated, SecurityResponse.class);
    }

    @Override
    public void deleteSecurity(Long securityId) {
        if (!securityRepository.existsById(securityId)) {
            throw new ResourceNotFoundException(
                    "Security not found with id: " + securityId);
        }
        securityRepository.deleteById(securityId);
    }
}