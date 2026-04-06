package com.wealthpro.productcatalog.service;

import com.wealthpro.productcatalog.dto.request.ProductTermRequest;
import com.wealthpro.productcatalog.dto.response.ProductTermResponse;
import com.wealthpro.productcatalog.entity.ProductTerm;
import com.wealthpro.productcatalog.entity.Security;
import com.wealthpro.productcatalog.enums.AssetClass;
import com.wealthpro.productcatalog.enums.SecurityStatus;
import com.wealthpro.productcatalog.exception.ResourceNotFoundException;
import com.wealthpro.productcatalog.repository.ProductTermRepository;
import com.wealthpro.productcatalog.repository.SecurityRepository;
import com.wealthpro.productcatalog.service.ProductTermServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductTermServiceTest {

    @Mock
    private ProductTermRepository productTermRepository;

    @Mock
    private SecurityRepository securityRepository;

    @InjectMocks
    private ProductTermServiceImpl productTermService;

    private Security security;
    private ProductTerm productTerm;
    private ProductTermRequest request;

    @BeforeEach
    void setUp() {
        security = new Security();
        security.setSecurityId(1L);
        security.setSymbol("AAPL");
        security.setAssetClass(AssetClass.EQUITY);
        security.setCurrency("USD");
        security.setCountry("USA");
        security.setStatus(SecurityStatus.ACTIVE);

        productTerm = new ProductTerm();
        productTerm.setTermId(1L);
        productTerm.setSecurity(security);
        productTerm.setTermJson("{\"minHold\": 30}");
        productTerm.setEffectiveFrom(LocalDate.of(2024, 1, 1));
        productTerm.setEffectiveTo(LocalDate.of(2024, 12, 31));

        request = new ProductTermRequest();
        request.setSecurityId(1L);
        request.setTermJson("{\"minHold\": 30}");
        request.setEffectiveFrom(LocalDate.of(2024, 1, 1));
        request.setEffectiveTo(LocalDate.of(2024, 12, 31));
    }

    @Test
    void shouldCreateProductTermSuccessfully() {
        when(securityRepository.findById(1L)).thenReturn(Optional.of(security));
        when(productTermRepository.save(any(ProductTerm.class))).thenReturn(productTerm);

        ProductTermResponse result = productTermService.createProductTerm(request);

        assertNotNull(result);
        assertEquals(1L, result.getTermId());
        assertEquals("{\"minHold\": 30}", result.getTermJson());
        assertEquals("AAPL", result.getSecuritySymbol());
        verify(productTermRepository).save(any(ProductTerm.class));
    }

    @Test
    void shouldThrowNotFoundWhenSecurityNotFoundOnCreate() {
        when(securityRepository.findById(99L)).thenReturn(Optional.empty());
        request.setSecurityId(99L);

        assertThrows(ResourceNotFoundException.class,
                () -> productTermService.createProductTerm(request));

        verify(productTermRepository, never()).save(any());
    }

    @Test
    void shouldGetProductTermByIdSuccessfully() {
        when(productTermRepository.findById(1L)).thenReturn(Optional.of(productTerm));

        ProductTermResponse result = productTermService.getProductTermById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getTermId());
        assertEquals(1L, result.getSecurityId());
    }

    @Test
    void shouldThrowNotFoundWhenTermIdNotFound() {
        when(productTermRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productTermService.getProductTermById(99L));
    }

    @Test
    void shouldGetAllProductTerms() {
        when(productTermRepository.findAll()).thenReturn(List.of(productTerm));

        List<ProductTermResponse> result = productTermService.getAllProductTerms();

        assertEquals(1, result.size());
        assertEquals("{\"minHold\": 30}", result.get(0).getTermJson());
    }

    @Test
    void shouldGetProductTermsBySecurityId() {
        when(securityRepository.existsById(1L)).thenReturn(true);
        when(productTermRepository.findBySecuritySecurityId(1L)).thenReturn(List.of(productTerm));

        List<ProductTermResponse> result = productTermService.getProductTermsBySecurityId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getSecurityId());
    }

    @Test
    void shouldThrowNotFoundWhenSecurityNotFoundOnGetBySecurityId() {
        when(securityRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> productTermService.getProductTermsBySecurityId(99L));
    }

    @Test
    void shouldGetOpenEndedProductTerms() {
        ProductTerm openTerm = new ProductTerm();
        openTerm.setTermId(2L);
        openTerm.setSecurity(security);
        openTerm.setTermJson("{\"minHold\": 90}");
        openTerm.setEffectiveFrom(LocalDate.of(2025, 1, 1));
        openTerm.setEffectiveTo(null);

        when(productTermRepository.findByEffectiveToIsNull()).thenReturn(List.of(openTerm));

        List<ProductTermResponse> result = productTermService.getOpenEndedProductTerms();

        assertEquals(1, result.size());
        assertNull(result.get(0).getEffectiveTo());
    }

    @Test
    void shouldUpdateProductTermSuccessfully() {
        when(productTermRepository.findById(1L)).thenReturn(Optional.of(productTerm));
        when(securityRepository.findById(1L)).thenReturn(Optional.of(security));
        when(productTermRepository.save(any(ProductTerm.class))).thenReturn(productTerm);

        ProductTermResponse result = productTermService.updateProductTerm(1L, request);

        assertNotNull(result);
        verify(productTermRepository).save(any(ProductTerm.class));
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentTerm() {
        when(productTermRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productTermService.updateProductTerm(99L, request));
    }

    @Test
    void shouldDeleteProductTermSuccessfully() {
        when(productTermRepository.existsById(1L)).thenReturn(true);

        productTermService.deleteProductTerm(1L);

        verify(productTermRepository).deleteById(1L);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentTerm() {
        when(productTermRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> productTermService.deleteProductTerm(99L));

        verify(productTermRepository, never()).deleteById(any());
    }
}