package com.wealth.goalsadvisory.servicetestcases;

import com.wealth.goalsadvisory.dto.request.RecommendationRequest;
import com.wealth.goalsadvisory.dto.response.RecommendationResponse;
import com.wealth.goalsadvisory.entity.ModelPortfolio;
import com.wealth.goalsadvisory.entity.Recommendation;
import com.wealth.goalsadvisory.enums.ModelPortfolioStatus;
import com.wealth.goalsadvisory.enums.RecommendationStatus;
import com.wealth.goalsadvisory.enums.RiskClass;
import com.wealth.goalsadvisory.exception.ResourceNotFoundException;
import com.wealth.goalsadvisory.repository.ModelPortfolioRepository;
import com.wealth.goalsadvisory.repository.RecommendationRepository;
import com.wealth.goalsadvisory.service.Impl.RecommendationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private ModelPortfolioRepository modelPortfolioRepository;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    private Recommendation sampleRecommendation;
    private RecommendationResponse sampleResponse;
    private ModelPortfolio samplePortfolio;
    private RecommendationRequest sampleRequest;

    @BeforeEach
    void setUp() {
        samplePortfolio = new ModelPortfolio();
        samplePortfolio.setModelId(1L);
        samplePortfolio.setName("Balanced Growth Portfolio");
        samplePortfolio.setRiskClass(RiskClass.BALANCED);
        samplePortfolio.setWeightsJson("{\"Equity\": 60, \"Bond\": 30, \"Cash\": 10}");
        samplePortfolio.setStatus(ModelPortfolioStatus.ACTIVE);

        sampleRecommendation = new Recommendation();
        sampleRecommendation.setRecoId(1L);
        sampleRecommendation.setClientId(101L);
        sampleRecommendation.setModelPortfolio(samplePortfolio);
        sampleRecommendation.setProposalJson("{\"notes\": \"Balanced suits client\"}");
        sampleRecommendation.setProposedDate(LocalDate.now());
        sampleRecommendation.setStatus(RecommendationStatus.DRAFT);

        sampleResponse = new RecommendationResponse();
        sampleResponse.setRecoId(1L);
        sampleResponse.setClientId(101L);
        sampleResponse.setModelId(1L);
        sampleResponse.setModelName("Balanced Growth Portfolio");
        sampleResponse.setRiskClass(RiskClass.BALANCED);
        sampleResponse.setProposalJson("{\"notes\": \"Balanced suits client\"}");
        sampleResponse.setProposedDate(LocalDate.now());
        sampleResponse.setStatus(RecommendationStatus.DRAFT);

        sampleRequest = new RecommendationRequest();
        sampleRequest.setClientId(101L);
        sampleRequest.setRiskClass(RiskClass.BALANCED);
        sampleRequest.setProposalJson("{\"notes\": \"Balanced suits client\"}");
        sampleRequest.setProposedDate(LocalDate.now());
    }
//recommendation is created when modelportfolio is there
    @Test
    void createRecommendation_positive() {
        when(modelPortfolioRepository.findByRiskClassAndStatus(RiskClass.BALANCED, ModelPortfolioStatus.ACTIVE)).thenReturn(List.of(samplePortfolio));
        when(recommendationRepository.save(any(Recommendation.class))).thenReturn(sampleRecommendation);
        when(mapper.map(any(Recommendation.class), eq(RecommendationResponse.class))).thenReturn(sampleResponse);
        RecommendationResponse response = recommendationService.createRecommendation(sampleRequest);

        assertNotNull(response);
        assertEquals(101L, response.getClientId());
        assertEquals("Balanced Growth Portfolio", response.getModelName());
        assertEquals(RecommendationStatus.DRAFT, response.getStatus());
    }

    @Test
    void createRecommendation_negative() {
        when(modelPortfolioRepository.findByRiskClassAndStatus(RiskClass.BALANCED, ModelPortfolioStatus.ACTIVE)).thenReturn(List.of());
        assertThrows(ResourceNotFoundException.class, () -> recommendationService.createRecommendation(sampleRequest));
        verify(recommendationRepository, never()).save(any());
    }
//status should be draft at first when not provided
    @Test
    void createRecommendation_defaultStatus() {
        sampleRequest.setStatus(null);
        when(modelPortfolioRepository.findByRiskClassAndStatus(RiskClass.BALANCED, ModelPortfolioStatus.ACTIVE)).thenReturn(List.of(samplePortfolio));
        when(recommendationRepository.save(any(Recommendation.class))).thenReturn(sampleRecommendation);
        when(mapper.map(any(Recommendation.class), eq(RecommendationResponse.class))).thenReturn(sampleResponse);
        RecommendationResponse response = recommendationService.createRecommendation(sampleRequest);
        assertEquals(RecommendationStatus.DRAFT, response.getStatus());
    }

    @Test
    void getRecommendationById_positive() {
        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(sampleRecommendation));
        when(mapper.map(any(Recommendation.class), eq(RecommendationResponse.class))).thenReturn(sampleResponse);
        RecommendationResponse response = recommendationService.getRecommendationById(1L);
        assertNotNull(response);
        assertEquals(1L, response.getRecoId());
    }

    @Test
    void getRecommendationById_negative() {
        when(recommendationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> recommendationService.getRecommendationById(99L));
    }

    @Test
    void getRecommendationsByClientId() {
        when(recommendationRepository.findByClientId(101L)).thenReturn(List.of(sampleRecommendation));
        when(mapper.map(any(Recommendation.class), eq(RecommendationResponse.class))).thenReturn(sampleResponse);
        List<RecommendationResponse> responses = recommendationService.getRecommendationsByClientId(101L);
        assertEquals(1, responses.size());
        assertEquals(101L, responses.get(0).getClientId());
    }
// Current must be DRAFT so DRAFT → SUBMITTED is valid
    @Test
    void updateRecommendationStatus_valid() {

        sampleRecommendation.setStatus(RecommendationStatus.DRAFT);
        sampleResponse.setStatus(RecommendationStatus.SUBMITTED);

        when(recommendationRepository.findById(1L)).thenReturn(Optional.of(sampleRecommendation));
        when(recommendationRepository.save(any(Recommendation.class))).thenReturn(sampleRecommendation);
        when(mapper.map(any(Recommendation.class), eq(RecommendationResponse.class))).thenReturn(sampleResponse);
        RecommendationResponse response = recommendationService.updateRecommendationStatus(1L, RecommendationStatus.SUBMITTED);
        assertEquals(RecommendationStatus.SUBMITTED, response.getStatus());
    }
//throws exception when updation is not done correctly
    @Test
    void updateRecommendationStatus_Invalid() {
        sampleRecommendation.setStatus(RecommendationStatus.APPROVED);
        when(recommendationRepository.findById(1L))
                .thenReturn(Optional.of(sampleRecommendation));

        assertThrows(IllegalArgumentException.class,
                () -> recommendationService.updateRecommendationStatus(
                        1L, RecommendationStatus.DRAFT));
    }
//the deletion should be done when the status is draft
    @Test
    void deleteRecommendation_draft() {
        when(recommendationRepository.findById(1L))
                .thenReturn(Optional.of(sampleRecommendation));
        doNothing().when(recommendationRepository).delete(sampleRecommendation);

        assertDoesNotThrow(() ->
                recommendationService.deleteRecommendation(1L));

        verify(recommendationRepository, times(1)).delete(sampleRecommendation);
    }

    @Test
    void deleteRecommendation_NotDraft() {
        sampleRecommendation.setStatus(RecommendationStatus.APPROVED);
        when(recommendationRepository.findById(1L))
                .thenReturn(Optional.of(sampleRecommendation));

        assertThrows(IllegalArgumentException.class,
                () -> recommendationService.deleteRecommendation(1L));

        verify(recommendationRepository, never()).delete(any());
    }
//when the recommendation is not found
    @Test
    void deleteRecommendation() {
        when(recommendationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> recommendationService.deleteRecommendation(99L));

        verify(recommendationRepository, never()).delete(any());
    }
}
