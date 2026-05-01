package com.wealthpro.productcatalog.repository;

import com.wealthpro.productcatalog.entity.Security;
import com.wealthpro.productcatalog.enums.AssetClass;
import com.wealthpro.productcatalog.enums.SecurityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class SecurityRepositoryTest {

    @Autowired
    private SecurityRepository securityRepository;

    private Security equity;
    private Security bond;

    @BeforeEach
    void setUp() {
        securityRepository.deleteAll();

        equity = new Security();
        equity.setSymbol("AAPL");
        equity.setAssetClass(AssetClass.EQUITY);
        equity.setCurrency("USD");
        equity.setCountry("USA");
        equity.setStatus(SecurityStatus.ACTIVE);
        securityRepository.save(equity);

        bond = new Security();
        bond.setSymbol("US10Y");
        bond.setAssetClass(AssetClass.BOND);
        bond.setCurrency("USD");
        bond.setCountry("USA");
        bond.setStatus(SecurityStatus.INACTIVE);
        securityRepository.save(bond);
    }

    @Test
    void shouldSaveAndFindById() {
        Optional<Security> found = securityRepository.findById(equity.getSecurityId());

        assertTrue(found.isPresent());
        assertEquals("AAPL", found.get().getSymbol());
    }

    @Test
    void shouldFindBySymbol() {
        Optional<Security> found = securityRepository.findBySymbol("AAPL");

        assertTrue(found.isPresent());
        assertEquals(AssetClass.EQUITY, found.get().getAssetClass());
    }

    @Test
    void shouldReturnEmptyWhenSymbolNotFound() {
        Optional<Security> found = securityRepository.findBySymbol("UNKNOWN");

        assertFalse(found.isPresent());
    }

    @Test
    void shouldReturnTrueWhenSymbolExists() {
        boolean exists = securityRepository.existsBySymbol("AAPL");

        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenSymbolDoesNotExist() {
        boolean exists = securityRepository.existsBySymbol("NOTEXIST");

        assertFalse(exists);
    }

    @Test
    void shouldFindByAssetClass() {
        List<Security> equities = securityRepository.findByAssetClass(AssetClass.EQUITY);

        assertEquals(1, equities.size());
        assertEquals("AAPL", equities.get(0).getSymbol());
    }

    @Test
    void shouldFindByStatus() {
        List<Security> active = securityRepository.findByStatus(SecurityStatus.ACTIVE);

        assertEquals(1, active.size());
        assertEquals("AAPL", active.get(0).getSymbol());
    }

    @Test
    void shouldFindByCountry() {
        List<Security> usaSecurities = securityRepository.findByCountry("USA");

        assertEquals(2, usaSecurities.size());
    }

    @Test
    void shouldFindByAssetClassAndStatus() {
        List<Security> result = securityRepository
                .findByAssetClassAndStatus(AssetClass.EQUITY, SecurityStatus.ACTIVE);

        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
    }

    @Test
    void shouldFindAllSecurities() {
        List<Security> all = securityRepository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void shouldDeleteById() {
        securityRepository.deleteById(equity.getSecurityId());

        assertFalse(securityRepository.existsById(equity.getSecurityId()));
    }
}