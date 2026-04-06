package com.wealthpro.productcatalog.controller;

import com.wealthpro.productcatalog.controller.ProductTermController;
import com.wealthpro.productcatalog.dto.request.ProductTermRequest;
import com.wealthpro.productcatalog.dto.response.ProductTermResponse;
import com.wealthpro.productcatalog.service.ProductTermService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/product-terms")
public class ProductTermControllerImpl implements ProductTermController {

    private final ProductTermService productTermService;

    public ProductTermControllerImpl(ProductTermService productTermService) {
        this.productTermService = productTermService;
    }

    @Override
    @PostMapping
    public ResponseEntity<ProductTermResponse> createProductTerm(
            @Valid @RequestBody ProductTermRequest request) {
        ProductTermResponse response = productTermService.createProductTerm(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/{termId}")
    public ResponseEntity<ProductTermResponse> getProductTermById(
            @PathVariable Long termId) {
        ProductTermResponse response = productTermService.getProductTermById(termId);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<ProductTermResponse>> getAllProductTerms() {
        List<ProductTermResponse> responses = productTermService.getAllProductTerms();
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/security/{securityId}")
    public ResponseEntity<List<ProductTermResponse>> getProductTermsBySecurityId(
            @PathVariable Long securityId) {
        List<ProductTermResponse> responses = productTermService.getProductTermsBySecurityId(securityId);
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/active")
    public ResponseEntity<List<ProductTermResponse>> getActiveProductTerms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        List<ProductTermResponse> responses = productTermService.getActiveProductTerms(asOfDate);
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/open-ended")
    public ResponseEntity<List<ProductTermResponse>> getOpenEndedProductTerms() {
        List<ProductTermResponse> responses = productTermService.getOpenEndedProductTerms();
        return ResponseEntity.ok(responses);
    }

    @Override
    @PutMapping("/{termId}")
    public ResponseEntity<ProductTermResponse> updateProductTerm(
            @PathVariable Long termId,
            @Valid @RequestBody ProductTermRequest request) {
        ProductTermResponse response = productTermService.updateProductTerm(termId, request);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{termId}")
    public ResponseEntity deleteProductTerm(@PathVariable Long termId) {
        productTermService.deleteProductTerm(termId);
        return ResponseEntity.ok("Product Term Deleted Succesfully");
    }
}