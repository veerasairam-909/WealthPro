package com.wealthpro.productcatalog.service;

import com.wealthpro.productcatalog.dto.request.SecurityRequest;
import com.wealthpro.productcatalog.dto.response.SecurityResponse;
import com.wealthpro.productcatalog.entity.Security;
import com.wealthpro.productcatalog.enums.AssetClass;
import com.wealthpro.productcatalog.enums.SecurityStatus;
import com.wealthpro.productcatalog.exception.DuplicateResourceException;
import com.wealthpro.productcatalog.exception.ResourceNotFoundException;
import com.wealthpro.productcatalog.repository.SecurityRepository;
import com.wealthpro.productcatalog.service.SecurityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock
    private SecurityRepository securityRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SecurityServiceImpl securityService;

    private Security security;
    private SecurityRequest request;
    private SecurityResponse response;

    @BeforeEach
    void setUp() {
        security = new Security();
        security.setSecurityId(1L);
        security.setSymbol("AAPL");
        security.setAssetClass(AssetClass.EQUITY);
        security.setCurrency("USD");
        security.setCountry("USA");
        security.setStatus(SecurityStatus.ACTIVE);

        request = new SecurityRequest();
        request.setSymbol("AAPL");
        request.setAssetClass(AssetClass.EQUITY);
        request.setCurrency("USD");
        request.setCountry("USA");
        request.setStatus(SecurityStatus.ACTIVE);

        response = new SecurityResponse();
        response.setSecurityId(1L);
        response.setSymbol("AAPL");
        response.setAssetClass(AssetClass.EQUITY);
        response.setCurrency("USD");
        response.setCountry("USA");
        response.setStatus(SecurityStatus.ACTIVE);
    }

    @Test
    void shouldCreateSecuritySuccessfully() {
        when(securityRepository.existsBySymbolIgnoreCase("AAPL")).thenReturn(false);
        when(modelMapper.map(request, Security.class)).thenReturn(security);
        when(securityRepository.save(security)).thenReturn(security);
        when(modelMapper.map(security, SecurityResponse.class)).thenReturn(response);

        SecurityResponse result = securityService.createSecurity(request);

        assertNotNull(result);
        assertEquals("AAPL", result.getSymbol());
        verify(securityRepository).save(security);
    }

    @Test
    void shouldThrowDuplicateExceptionWhenSymbolExists() {
        when(securityRepository.existsBySymbolIgnoreCase("AAPL")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> securityService.createSecurity(request));

        verify(securityRepository, never()).save(any());
    }

    @Test
    void shouldGetSecurityByIdSuccessfully() {
        when(securityRepository.findById(1L)).thenReturn(Optional.of(security));
        when(modelMapper.map(security, SecurityResponse.class)).thenReturn(response);

        SecurityResponse result = securityService.getSecurityById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getSecurityId());
    }

    @Test
    void shouldThrowNotFoundWhenSecurityIdNotFound() {
        when(securityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> securityService.getSecurityById(99L));
    }

    @Test
    void shouldGetSecurityBySymbolSuccessfully() {
        when(securityRepository.findBySymbolIgnoreCase("AAPL")).thenReturn(Optional.of(security));
        when(modelMapper.map(security, SecurityResponse.class)).thenReturn(response);

        SecurityResponse result = securityService.getSecurityBySymbol("AAPL");

        assertNotNull(result);
        assertEquals("AAPL", result.getSymbol());
    }

    @Test
    void shouldThrowNotFoundWhenSymbolNotFound() {
        when(securityRepository.findBySymbolIgnoreCase("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> securityService.getSecurityBySymbol("UNKNOWN"));
    }

    @Test
    void shouldGetAllSecurities() {
        when(securityRepository.findAll()).thenReturn(List.of(security));
        when(modelMapper.map(security, SecurityResponse.class)).thenReturn(response);

        List<SecurityResponse> result = securityService.getAllSecurities();

        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
    }

    @Test
    void shouldGetSecuritiesByAssetClass() {
        when(securityRepository.findByAssetClass(AssetClass.EQUITY)).thenReturn(List.of(security));
        when(modelMapper.map(security, SecurityResponse.class)).thenReturn(response);

        List<SecurityResponse> result = securityService.getSecuritiesByAssetClass(AssetClass.EQUITY);

        assertEquals(1, result.size());
    }

    @Test
    void shouldGetSecuritiesByStatus() {
        when(securityRepository.findByStatus(SecurityStatus.ACTIVE)).thenReturn(List.of(security));
        when(modelMapper.map(security, SecurityResponse.class)).thenReturn(response);

        List<SecurityResponse> result = securityService.getSecuritiesByStatus(SecurityStatus.ACTIVE);

        assertEquals(1, result.size());
    }

    @Test
    void shouldUpdateSecuritySuccessfully() {
        SecurityRequest updateRequest = new SecurityRequest();
        updateRequest.setSymbol("AAPL");
        updateRequest.setAssetClass(AssetClass.EQUITY);
        updateRequest.setCurrency("EUR");
        updateRequest.setCountry("USA");
        updateRequest.setStatus(SecurityStatus.ACTIVE);

        when(securityRepository.findById(1L)).thenReturn(Optional.of(security));
        when(securityRepository.save(security)).thenReturn(security);
        when(modelMapper.map(security, SecurityResponse.class)).thenReturn(response);

        SecurityResponse result = securityService.updateSecurity(1L, updateRequest);

        assertNotNull(result);
        verify(securityRepository).save(security);
    }

    @Test
    void shouldThrowDuplicateOnUpdateWhenSymbolTakenByAnother() {
        SecurityRequest updateRequest = new SecurityRequest();
        updateRequest.setSymbol("TSLA");
        updateRequest.setAssetClass(AssetClass.EQUITY);
        updateRequest.setCurrency("USD");
        updateRequest.setCountry("USA");
        updateRequest.setStatus(SecurityStatus.ACTIVE);

        when(securityRepository.findById(1L)).thenReturn(Optional.of(security));
        when(securityRepository.existsBySymbolIgnoreCase("TSLA")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> securityService.updateSecurity(1L, updateRequest));
    }

    @Test
    void shouldDeleteSecuritySuccessfully() {
        when(securityRepository.existsById(1L)).thenReturn(true);

        securityService.deleteSecurity(1L);

        verify(securityRepository).deleteById(1L);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentSecurity() {
        when(securityRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> securityService.deleteSecurity(99L));

        verify(securityRepository, never()).deleteById(any());
    }
}