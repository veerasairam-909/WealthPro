package com.wealth.goalsadvisory.repositorytestcases;

import com.wealth.goalsadvisory.entity.ModelPortfolio;
import com.wealth.goalsadvisory.enums.ModelPortfolioStatus;
import com.wealth.goalsadvisory.enums.RiskClass;
import com.wealth.goalsadvisory.repository.ModelPortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ModelPortfolioRepositoryTest {

    @Autowired
    private ModelPortfolioRepository modelPortfolioRepository;

    private ModelPortfolio portfolio1;
    private ModelPortfolio portfolio2;

    @BeforeEach
    void setUp() {
        modelPortfolioRepository.deleteAll();

        portfolio1 = new ModelPortfolio();
        portfolio1.setName("Balanced Growth");
        portfolio1.setRiskClass(RiskClass.BALANCED);
        portfolio1.setWeightsJson("{\"Equity\": 60, \"Bond\": 30, \"Cash\": 10}");
        portfolio1.setStatus(ModelPortfolioStatus.ACTIVE);

        portfolio2 = new ModelPortfolio();
        portfolio2.setName("Aggressive Growth");
        portfolio2.setRiskClass(RiskClass.AGGRESSIVE);
        portfolio2.setWeightsJson("{\"Equity\": 85, \"Bond\": 10, \"Cash\": 5}");
        portfolio2.setStatus(ModelPortfolioStatus.INACTIVE);

        modelPortfolioRepository.save(portfolio1);
        modelPortfolioRepository.save(portfolio2);
    }

    @Test
    void findById_WhenAvaliable() {
        Optional<ModelPortfolio> found = modelPortfolioRepository.findById(portfolio1.getModelId());

        assertTrue(found.isPresent());
        assertEquals("Balanced Growth", found.get().getName());
    }

    @Test
    void findAll_Portfolios() {
        List<ModelPortfolio> portfolios = modelPortfolioRepository.findAll();

        assertEquals(2, portfolios.size());
    }

    @Test
    void delete_Portfolio() {
        modelPortfolioRepository.delete(portfolio1);

        Optional<ModelPortfolio> found = modelPortfolioRepository.findById(portfolio1.getModelId());
        assertFalse(found.isPresent());
    }


    @Test
    void findByRiskClass_Portfolios() {
        List<ModelPortfolio> portfolios = modelPortfolioRepository.findByRiskClass(RiskClass.BALANCED);

        assertEquals(1, portfolios.size());
        assertEquals("Balanced Growth", portfolios.get(0).getName());
    }

    @Test
    void findByRiskClass_WhenNegative() {
        List<ModelPortfolio> portfolios = modelPortfolioRepository.findByRiskClass(RiskClass.CONSERVATIVE);

        assertTrue(portfolios.isEmpty());
    }

    @Test
    void findByStatus_ActivePortfolios() {
        List<ModelPortfolio> portfolios = modelPortfolioRepository.findByStatus(ModelPortfolioStatus.ACTIVE);

        assertEquals(1, portfolios.size());
        assertEquals("Balanced Growth", portfolios.get(0).getName());
    }

    @Test
    void findByStatus_InactivePortfolios() {
        List<ModelPortfolio> portfolios = modelPortfolioRepository.findByStatus(ModelPortfolioStatus.INACTIVE);

        assertEquals(1, portfolios.size());
        assertEquals("Aggressive Growth", portfolios.get(0).getName());
    }

    @Test
    void findByName_WhenNameExists() {
        Optional<ModelPortfolio> found = modelPortfolioRepository.findByName("Balanced Growth");

        assertTrue(found.isPresent());
        assertEquals(RiskClass.BALANCED, found.get().getRiskClass());
    }

    @Test
    void findByName_WhenNameNotExists() {
        Optional<ModelPortfolio> found = modelPortfolioRepository.findByName("Unknown Portfolio");

        assertFalse(found.isPresent());
    }

    @Test
    void existsByName_WhenNameExists() {
        boolean exists = modelPortfolioRepository.existsByName("Balanced Growth");

        assertTrue(exists);
    }
    @Test
    void existsByName_WhenNameNotExists() {
        boolean exists = modelPortfolioRepository.existsByName("Unknown Portfolio");

        assertFalse(exists);
    }
}