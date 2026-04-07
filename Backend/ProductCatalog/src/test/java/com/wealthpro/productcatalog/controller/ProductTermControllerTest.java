package com.wealthpro.productcatalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wealthpro.productcatalog.controller.ProductTermControllerImpl;
import com.wealthpro.productcatalog.dto.request.ProductTermRequest;
import com.wealthpro.productcatalog.dto.response.ProductTermResponse;
import com.wealthpro.productcatalog.exception.GlobalExceptionHandler;
import com.wealthpro.productcatalog.exception.ResourceNotFoundException;
import com.wealthpro.productcatalog.service.ProductTermService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductTermControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ProductTermService productTermService;

    @InjectMocks
    private ProductTermControllerImpl productTermController;

    private ProductTermRequest request;
    private ProductTermResponse response;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(productTermController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        request = new ProductTermRequest();
        request.setSecurityId(1L);
        request.setTermJson("{\"minHold\": 30}");
        request.setEffectiveFrom(LocalDate.of(2024, 1, 1));
        request.setEffectiveTo(LocalDate.of(2024, 12, 31));

        response = new ProductTermResponse();
        response.setTermId(1L);
        response.setSecurityId(1L);
        response.setSecuritySymbol("AAPL");
        response.setTermJson("{\"minHold\": 30}");
        response.setEffectiveFrom(LocalDate.of(2024, 1, 1));
        response.setEffectiveTo(LocalDate.of(2024, 12, 31));
    }

    @Test
    void shouldCreateProductTermAndReturn201() throws Exception {
        when(productTermService.createProductTerm(any(ProductTermRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/product-terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.termId").value(1))
                .andExpect(jsonPath("$.securitySymbol").value("AAPL"))
                .andExpect(jsonPath("$.termJson").value("{\"minHold\": 30}"));
    }

    @Test
    void shouldReturn400WhenRequestBodyInvalid() throws Exception {
        ProductTermRequest invalid = new ProductTermRequest();

        mockMvc.perform(post("/api/product-terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetProductTermByIdAndReturn200() throws Exception {
        when(productTermService.getProductTermById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/product-terms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.termId").value(1))
                .andExpect(jsonPath("$.securityId").value(1));
    }

    @Test
    void shouldReturn404WhenTermIdNotFound() throws Exception {
        when(productTermService.getProductTermById(99L))
                .thenThrow(new ResourceNotFoundException("ProductTerm not found with id: 99"));

        mockMvc.perform(get("/api/product-terms/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldGetAllProductTermsAndReturn200() throws Exception {
        when(productTermService.getAllProductTerms()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/product-terms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetProductTermsBySecurityId() throws Exception {
        when(productTermService.getProductTermsBySecurityId(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/product-terms/security/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetOpenEndedProductTerms() throws Exception {
        ProductTermResponse openResponse = new ProductTermResponse();
        openResponse.setTermId(2L);
        openResponse.setSecurityId(1L);
        openResponse.setSecuritySymbol("AAPL");
        openResponse.setTermJson("{\"minHold\": 90}");
        openResponse.setEffectiveFrom(LocalDate.of(2025, 1, 1));
        openResponse.setEffectiveTo(null);

        when(productTermService.getOpenEndedProductTerms()).thenReturn(List.of(openResponse));

        mockMvc.perform(get("/api/product-terms/open-ended"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldUpdateProductTermAndReturn200() throws Exception {
        when(productTermService.updateProductTerm(eq(1L), any(ProductTermRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/product-terms/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.termId").value(1));
    }

    @Test
    void shouldDeleteProductTermAndReturn204() throws Exception {
        doNothing().when(productTermService).deleteProductTerm(1L);

        mockMvc.perform(delete("/api/product-terms/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentTerm() throws Exception {
        doThrow(new ResourceNotFoundException("ProductTerm not found with id: 99"))
                .when(productTermService).deleteProductTerm(99L);

        mockMvc.perform(delete("/api/product-terms/99"))
                .andExpect(status().isNotFound());
    }
}