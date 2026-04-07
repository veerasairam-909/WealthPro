package com.wealthpro.productcatalog.controller;

import com.wealthpro.productcatalog.dto.request.ProductTermRequest;
import com.wealthpro.productcatalog.dto.response.ProductTermResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

public interface ProductTermController {

    @PostMapping
    ResponseEntity<ProductTermResponse> createProductTerm(@Valid @RequestBody ProductTermRequest request);

    @GetMapping("/{termId}")
    ResponseEntity<ProductTermResponse> getProductTermById(@PathVariable Long termId);

    @GetMapping
    ResponseEntity<List<ProductTermResponse>> getAllProductTerms();

    @GetMapping("/security/{securityId}")
    ResponseEntity<List<ProductTermResponse>> getProductTermsBySecurityId(@PathVariable Long securityId);

    @GetMapping("/active")
    ResponseEntity<List<ProductTermResponse>> getActiveProductTerms(@RequestParam LocalDate asOfDate);

    @GetMapping("/open-ended")
    ResponseEntity<List<ProductTermResponse>> getOpenEndedProductTerms();

    @PutMapping("/{termId}")
    ResponseEntity<ProductTermResponse> updateProductTerm(
            @PathVariable Long termId,
            @Valid @RequestBody ProductTermRequest request);

    @DeleteMapping("/{termId}")
    ResponseEntity deleteProductTerm(@PathVariable Long termId);
}