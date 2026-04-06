package com.wealthpro.productcatalog.service;

import com.wealthpro.productcatalog.dto.request.ProductTermRequest;
import com.wealthpro.productcatalog.dto.response.ProductTermResponse;
import com.wealthpro.productcatalog.entity.ProductTerm;
import com.wealthpro.productcatalog.entity.Security;
import com.wealthpro.productcatalog.exception.ResourceNotFoundException;
import com.wealthpro.productcatalog.repository.ProductTermRepository;
import com.wealthpro.productcatalog.repository.SecurityRepository;
import com.wealthpro.productcatalog.service.ProductTermService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductTermServiceImpl implements ProductTermService {

    private final ProductTermRepository productTermRepository;
    private final SecurityRepository securityRepository;

    public ProductTermServiceImpl(ProductTermRepository productTermRepository,
                                  SecurityRepository securityRepository) {
        this.productTermRepository = productTermRepository;
        this.securityRepository = securityRepository;
    }

    private Security fetchSecurity(Long securityId) {
        return securityRepository.findById(securityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Security not found with id: " + securityId));
    }

    private ProductTermResponse toResponse(ProductTerm term) {
        ProductTermResponse response = new ProductTermResponse();
        response.setTermId(term.getTermId());
        response.setSecurityId(term.getSecurity().getSecurityId());
        response.setSecuritySymbol(term.getSecurity().getSymbol());
        response.setTermJson(term.getTermJson());
        response.setEffectiveFrom(term.getEffectiveFrom());
        response.setEffectiveTo(term.getEffectiveTo());
        return response;
    }

    @Override
    public ProductTermResponse createProductTerm(ProductTermRequest request) {
        Security security = fetchSecurity(request.getSecurityId());
        ProductTerm term = new ProductTerm();
        term.setSecurity(security);
        term.setTermJson(request.getTermJson());
        term.setEffectiveFrom(request.getEffectiveFrom());
        term.setEffectiveTo(request.getEffectiveTo());
        ProductTerm saved = productTermRepository.save(term);
        return toResponse(saved);
    }

    @Override
    public ProductTermResponse getProductTermById(Long termId) {
        ProductTerm term = productTermRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ProductTerm not found with id: " + termId));
        return toResponse(term);
    }

    @Override
    public List<ProductTermResponse> getAllProductTerms() {
        List<ProductTerm> terms = productTermRepository.findAll();
        List<ProductTermResponse> responses = new ArrayList<>();
        for (ProductTerm term : terms) {
            responses.add(toResponse(term));
        }
        return responses;
    }

    @Override
    public List<ProductTermResponse> getProductTermsBySecurityId(Long securityId) {
        if (!securityRepository.existsById(securityId)) {
            throw new ResourceNotFoundException(
                    "Security not found with id: " + securityId);
        }
        List<ProductTerm> terms = productTermRepository.findBySecuritySecurityId(securityId);
        List<ProductTermResponse> responses = new ArrayList<>();
        for (ProductTerm term : terms) {
            responses.add(toResponse(term));
        }
        return responses;
    }

    @Override
    public List<ProductTermResponse> getActiveProductTerms(LocalDate asOfDate) {
        List<ProductTerm> terms = productTermRepository
                .findByEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(asOfDate, asOfDate);
        List<ProductTermResponse> responses = new ArrayList<>();
        for (ProductTerm term : terms) {
            responses.add(toResponse(term));
        }
        return responses;
    }

    @Override
    public List<ProductTermResponse> getOpenEndedProductTerms() {
        List<ProductTerm> terms = productTermRepository.findByEffectiveToIsNull();
        List<ProductTermResponse> responses = new ArrayList<>();
        for (ProductTerm term : terms) {
            responses.add(toResponse(term));
        }
        return responses;
    }

    @Override
    public ProductTermResponse updateProductTerm(Long termId, ProductTermRequest request) {
        ProductTerm existing = productTermRepository.findById(termId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ProductTerm not found with id: " + termId));
        Security security = fetchSecurity(request.getSecurityId());
        existing.setSecurity(security);
        existing.setTermJson(request.getTermJson());
        existing.setEffectiveFrom(request.getEffectiveFrom());
        existing.setEffectiveTo(request.getEffectiveTo());
        ProductTerm updated = productTermRepository.save(existing);
        return toResponse(updated);
    }

    @Override
    public void deleteProductTerm(Long termId) {
        if (!productTermRepository.existsById(termId)) {
            throw new ResourceNotFoundException(
                    "ProductTerm not found with id: " + termId);
        }
        productTermRepository.deleteById(termId);
    }
}