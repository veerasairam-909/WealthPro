package com.wealth.goalsadvisory.servicetestcases;

import com.wealth.goalsadvisory.dto.request.ModelPortfolioRequest;
import com.wealth.goalsadvisory.dto.response.ModelPortfolioResponse;
import com.wealth.goalsadvisory.entity.ModelPortfolio;
import com.wealth.goalsadvisory.enums.ModelPortfolioStatus;
import com.wealth.goalsadvisory.enums.RiskClass;
import com.wealth.goalsadvisory.exception.ResourceNotFoundException;
import com.wealth.goalsadvisory.repository.ModelPortfolioRepository;
import com.wealth.goalsadvisory.service.Impl.ModelPortfolioServiceImpl;
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
class ModelPortfolioServiceTest {

    @Mock
    private ModelPortfolioRepository modelPortfolioRepository;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private ModelPortfolioServiceImpl modelPortfolioService;

    private ModelPortfolio samplePortfolio;
    private ModelPortfolioResponse sampleResponse;
    private ModelPortfolioRequest sampleRequest;

    @BeforeEach
    void setUp() {
        samplePortfolio = new ModelPortfolio();
        samplePortfolio.setModelId(1L);
        samplePortfolio.setName("Balanced Growth Portfolio");
        samplePortfolio.setRiskClass(RiskClass.BALANCED);
        samplePortfolio.setWeightsJson("{\"Equity\": 60, \"Bond\": 30, \"Cash\": 10}");
        samplePortfolio.setStatus(ModelPortfolioStatus.ACTIVE);

        sampleResponse = new ModelPortfolioResponse();
        sampleResponse.setModelId(1L);
        sampleResponse.setName("Balanced Growth Portfolio");
        sampleResponse.setRiskClass(RiskClass.BALANCED);
        sampleResponse.setWeightsJson("{\"Equity\": 60, \"Bond\": 30, \"Cash\": 10}");
        sampleResponse.setStatus(ModelPortfolioStatus.ACTIVE);

        sampleRequest = new ModelPortfolioRequest();
        sampleRequest.setName("Balanced Growth Portfolio");
        sampleRequest.setRiskClass(RiskClass.BALANCED);
        sampleRequest.setWeightsJson("{\"Equity\": 60, \"Bond\": 30, \"Cash\": 10}");
        sampleRequest.setStatus(ModelPortfolioStatus.ACTIVE);
    }

    @Test
    void createModelPortfolio_positive() {
        when(modelPortfolioRepository.existsByName("Balanced Growth Portfolio")).thenReturn(false);
        when(modelPortfolioRepository.save(any(ModelPortfolio.class))).thenReturn(samplePortfolio);
        when(mapper.map(any(ModelPortfolio.class), eq(ModelPortfolioResponse.class))).thenReturn(sampleResponse);

        ModelPortfolioResponse response = modelPortfolioService.createModelPortfolio(sampleRequest);

        assertNotNull(response);
        assertEquals("Balanced Growth Portfolio", response.getName());
        assertEquals(RiskClass.BALANCED, response.getRiskClass());
    }

    @Test
    void createModelPortfolio_negative_alreadyExists() {
        when(modelPortfolioRepository.existsByName("Balanced Growth Portfolio")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> modelPortfolioService.createModelPortfolio(sampleRequest));

        verify(modelPortfolioRepository, never()).save(any(ModelPortfolio.class));
    }

    @Test
    void getModelPortfolioById_positive() {
        when(modelPortfolioRepository.findById(1L)).thenReturn(Optional.of(samplePortfolio));
        when(mapper.map(any(ModelPortfolio.class), eq(ModelPortfolioResponse.class))).thenReturn(sampleResponse);

        ModelPortfolioResponse response = modelPortfolioService.getModelPortfolioById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getModelId());
    }

    @Test
    void getModelPortfolioById_negative() {
        when(modelPortfolioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> modelPortfolioService.getModelPortfolioById(99L));
    }

    @Test
    void getModelPortfoliosByRiskClass() {
        when(modelPortfolioRepository.findByRiskClass(RiskClass.BALANCED))
                .thenReturn(List.of(samplePortfolio));
        when(mapper.map(any(ModelPortfolio.class), eq(ModelPortfolioResponse.class))).thenReturn(sampleResponse);

        List<ModelPortfolioResponse> responses =
                modelPortfolioService.getModelPortfoliosByRiskClass(RiskClass.BALANCED);

        assertEquals(1, responses.size());
        assertEquals(RiskClass.BALANCED, responses.get(0).getRiskClass());
    }
//when portfolio exists to delete
    @Test
    void deleteModelPortfolio_positive() {
        when(modelPortfolioRepository.findById(1L)).thenReturn(Optional.of(samplePortfolio));
        doNothing().when(modelPortfolioRepository).delete(samplePortfolio);

        assertDoesNotThrow(() -> modelPortfolioService.deleteModelPortfolio(1L));

        verify(modelPortfolioRepository, times(1)).delete(samplePortfolio);
    }
//when portfolio not exists to delete
    @Test
    void deleteModelPortfolio_negative() {
        when(modelPortfolioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> modelPortfolioService.deleteModelPortfolio(99L));
    }
}