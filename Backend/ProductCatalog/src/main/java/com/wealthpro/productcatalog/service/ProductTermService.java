package com.wealthpro.productcatalog.service;

import com.wealthpro.productcatalog.dto.request.ProductTermRequest;
import com.wealthpro.productcatalog.dto.response.ProductTermResponse;

import java.time.LocalDate;
import java.util.List;

public interface ProductTermService {

    ProductTermResponse createProductTerm(ProductTermRequest request);

    ProductTermResponse getProductTermById(Long termId);

    List<ProductTermResponse> getAllProductTerms();

    List<ProductTermResponse> getProductTermsBySecurityId(Long securityId);

    List<ProductTermResponse> getActiveProductTerms(LocalDate asOfDate);

    List<ProductTermResponse> getOpenEndedProductTerms();

    ProductTermResponse updateProductTerm(Long termId, ProductTermRequest request);

    void deleteProductTerm(Long termId);
}