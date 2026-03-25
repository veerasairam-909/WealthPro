package com.wealth.goalsadvisory.repositorytestcases;

import com.wealth.goalsadvisory.entity.ModelPortfolio;
import com.wealth.goalsadvisory.entity.Recommendation;
import com.wealth.goalsadvisory.enums.ModelPortfolioStatus;
import com.wealth.goalsadvisory.enums.RecommendationStatus;
import com.wealth.goalsadvisory.enums.RiskClass;
import com.wealth.goalsadvisory.repository.ModelPortfolioRepository;
import com.wealth.goalsadvisory.repository.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RecommendationRepositoryTest {

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private ModelPortfolioRepository modelPortfolioRepository;

    private ModelPortfolio savedPortfolio;
    private Recommendation recommendation1;
    private Recommendation recommendation2;

    @BeforeEach
    void setUp() {
        recommendationRepository.deleteAll();
        modelPortfolioRepository.deleteAll();

        ModelPortfolio portfolio = new ModelPortfolio();
        portfolio.setName("Balanced Growth");
        portfolio.setRiskClass(RiskClass.BALANCED);
        portfolio.setWeightsJson("{\"Equity\": 60, \"Bond\": 30, \"Cash\": 10}");
        portfolio.setStatus(ModelPortfolioStatus.ACTIVE);
        savedPortfolio = modelPortfolioRepository.save(portfolio);

        recommendation1 = new Recommendation();
        recommendation1.setClientId(101L);
        recommendation1.setModelPortfolio(savedPortfolio);
        recommendation1.setProposalJson("{\"notes\": \"Balanced suits client\"}");
        recommendation1.setProposedDate(LocalDate.now());
        recommendation1.setStatus(RecommendationStatus.DRAFT);
        recommendationRepository.save(recommendation1);

        recommendation2 = new Recommendation();
        recommendation2.setClientId(102L);
        recommendation2.setModelPortfolio(savedPortfolio);
        recommendation2.setProposalJson("{\"notes\": \"Growth strategy\"}");
        recommendation2.setProposedDate(LocalDate.now());
        recommendation2.setStatus(RecommendationStatus.SUBMITTED);
        recommendationRepository.save(recommendation2);
    }
    @Test
    void findById_WhenExists() {
        Optional<Recommendation> found =
                recommendationRepository.findById(recommendation1.getRecoId());
        assertTrue(found.isPresent());
        assertEquals(101L, found.get().getClientId());
    }

    @Test
    void findById_WhenNotExists() {
        assertFalse(recommendationRepository.findById(999L).isPresent());
    }

    @Test
    void findAll_ShouldReturnAllRecommendations() {
        assertEquals(2, recommendationRepository.findAll().size());
    }

    @Test
    void delete_RemoveRecommendation() {
        recommendationRepository.delete(recommendation1);
        assertFalse(recommendationRepository.findById(
                recommendation1.getRecoId()).isPresent());
    }

    @Test
    void findByClientId_ForClient() {
        List<Recommendation> recos = recommendationRepository.findByClientId(101L);
        assertEquals(1, recos.size());
        assertEquals(101L, recos.get(0).getClientId());
    }

    @Test
    void findByClientId_NoRecommendations() {
        assertTrue(recommendationRepository.findByClientId(999L).isEmpty());
    }

    @Test
    void findByClientIdAndStatus_FilteredRecommendations() {
        List<Recommendation> recos = recommendationRepository
                .findByClientIdAndStatus(101L, RecommendationStatus.DRAFT);
        assertEquals(1, recos.size());
        assertEquals(RecommendationStatus.DRAFT, recos.get(0).getStatus());
    }

    @Test
    void findByClientIdAndStatus_WhenNotSame() {
        assertTrue(recommendationRepository
                .findByClientIdAndStatus(101L, RecommendationStatus.APPROVED)
                .isEmpty());
    }

    @Test
    void findByModelPortfolio_ModelId_ShouldReturnRecosUsingModel() {
        List<Recommendation> recos = recommendationRepository
                .findByModelPortfolio_ModelId(savedPortfolio.getModelId());
        assertEquals(2, recos.size());
    }
}
