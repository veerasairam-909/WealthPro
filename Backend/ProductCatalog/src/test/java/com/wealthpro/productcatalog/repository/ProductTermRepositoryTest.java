package com.wealthpro.productcatalog.repository;

import com.wealthpro.productcatalog.entity.ProductTerm;
import com.wealthpro.productcatalog.entity.Security;
import com.wealthpro.productcatalog.enums.AssetClass;
import com.wealthpro.productcatalog.enums.SecurityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductTermRepositoryTest {

    @Autowired
    private ProductTermRepository productTermRepository;

    @Autowired
    private SecurityRepository securityRepository;

    private Security security;
    private ProductTerm activeTerm;
    private ProductTerm openEndedTerm;

    @BeforeEach
    void setUp() {
        productTermRepository.deleteAll();
        securityRepository.deleteAll();

        security = new Security();
        security.setSymbol("AAPL");
        security.setAssetClass(AssetClass.EQUITY);
        security.setCurrency("USD");
        security.setCountry("USA");
        security.setStatus(SecurityStatus.ACTIVE);
        securityRepository.save(security);

        activeTerm = new ProductTerm();
        activeTerm.setSecurity(security);
        activeTerm.setTermJson("{\"minHold\": 30}");
        activeTerm.setEffectiveFrom(LocalDate.of(2024, 1, 1));
        activeTerm.setEffectiveTo(LocalDate.of(2024, 12, 31));
        productTermRepository.save(activeTerm);

        openEndedTerm = new ProductTerm();
        openEndedTerm.setSecurity(security);
        openEndedTerm.setTermJson("{\"minHold\": 90}");
        openEndedTerm.setEffectiveFrom(LocalDate.of(2025, 1, 1));
        openEndedTerm.setEffectiveTo(null);
        productTermRepository.save(openEndedTerm);
    }

    @Test
    void shouldSaveAndFindById() {
        Optional<ProductTerm> found = productTermRepository.findById(activeTerm.getTermId());

        assertTrue(found.isPresent());
        assertEquals("{\"minHold\": 30}", found.get().getTermJson());
    }

    @Test
    void shouldFindBySecurityId() {
        List<ProductTerm> terms = productTermRepository
                .findBySecuritySecurityId(security.getSecurityId());

        assertEquals(2, terms.size());
    }

    @Test
    void shouldFindByEffectiveToIsNull() {
        List<ProductTerm> openEnded = productTermRepository.findByEffectiveToIsNull();

        assertEquals(1, openEnded.size());
        assertNull(openEnded.get(0).getEffectiveTo());
    }

    @Test
    void shouldFindByEffectiveFromBefore() {
        List<ProductTerm> terms = productTermRepository
                .findByEffectiveFromBefore(LocalDate.of(2024, 6, 1));

        assertEquals(1, terms.size());
        assertEquals(activeTerm.getTermId(), terms.get(0).getTermId());
    }

    @Test
    void shouldFindActiveTermsByDateRange() {
        List<ProductTerm> terms = productTermRepository
                .findByEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
                        LocalDate.of(2024, 6, 1),
                        LocalDate.of(2024, 6, 1));

        assertEquals(1, terms.size());
        assertEquals(activeTerm.getTermId(), terms.get(0).getTermId());
    }

    @Test
    void shouldFindAllTerms() {
        List<ProductTerm> all = productTermRepository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void shouldDeleteById() {
        productTermRepository.deleteById(activeTerm.getTermId());

        assertFalse(productTermRepository.existsById(activeTerm.getTermId()));
    }

    @Test
    void shouldReturnEmptyWhenSecurityHasNoTerms() {
        Security other = new Security();
        other.setSymbol("GOOG");
        other.setAssetClass(AssetClass.EQUITY);
        other.setCurrency("USD");
        other.setCountry("USA");
        other.setStatus(SecurityStatus.ACTIVE);
        securityRepository.save(other);

        List<ProductTerm> terms = productTermRepository
                .findBySecuritySecurityId(other.getSecurityId());

        assertTrue(terms.isEmpty());
    }
}