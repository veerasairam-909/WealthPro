package com.wealth.pbor.repositorytest;

import com.wealth.pbor.entity.Account;
import com.wealth.pbor.entity.Holding;
import com.wealth.pbor.enums.AccountStatus;
import com.wealth.pbor.enums.AccountType;
import com.wealth.pbor.repository.AccountRepository;
import com.wealth.pbor.repository.HoldingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class HoldingRepositoryTest {

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private AccountRepository accountRepository;

    private Account account;
    private Holding holding;

    @BeforeEach
    void setUp() {
        holdingRepository.deleteAll();
        accountRepository.deleteAll();

        account = new Account();
        account.setClientId(1L);
        account.setAccountType(AccountType.INDIVIDUAL);
        account.setBaseCurrency("INR");
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        holding = new Holding();
        holding.setAccount(account);
        holding.setSecurityId(101L);
        holding.setQuantity(new BigDecimal("100.0000"));
        holding.setAvgCost(new BigDecimal("250.0000"));
        holding.setValuationCurrency("INR");
        holding.setLastValuationDate(LocalDate.now());
        holdingRepository.save(holding);
    }

    @Test
    void testSaveHolding() {
        Holding newHolding = new Holding();
        newHolding.setAccount(account);
        newHolding.setSecurityId(102L);
        newHolding.setQuantity(new BigDecimal("50.0000"));
        newHolding.setAvgCost(new BigDecimal("300.0000"));
        newHolding.setValuationCurrency("INR");
        newHolding.setLastValuationDate(LocalDate.now());
        Holding saved = holdingRepository.save(newHolding);
        assertThat(saved.getHoldingId()).isNotNull();
    }

    @Test
    void testFindById() {
        Optional<Holding> found = holdingRepository.findById(holding.getHoldingId());
        assertThat(found).isPresent();
        assertThat(found.get().getSecurityId()).isEqualTo(101L);
    }

    @Test
    void testFindByAccountAccountId() {
        List<Holding> holdings = holdingRepository.findByAccountAccountId(account.getAccountId());
        assertThat(holdings).hasSize(1);
        assertThat(holdings.get(0).getSecurityId()).isEqualTo(101L);
    }

    @Test
    void testFindBySecurityId() {
        List<Holding> holdings = holdingRepository.findBySecurityId(101L);
        assertThat(holdings).hasSize(1);
    }

    @Test
    void testExistsByAccountAccountIdAndSecurityId() {
        boolean exists = holdingRepository.existsByAccountAccountIdAndSecurityId(account.getAccountId(), 101L);
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByAccountAccountIdAndSecurityId_NotFound() {
        boolean exists = holdingRepository.existsByAccountAccountIdAndSecurityId(account.getAccountId(), 999L);
        assertThat(exists).isFalse();
    }

    @Test
    void testDeleteHolding() {
        holdingRepository.delete(holding);
        Optional<Holding> found = holdingRepository.findById(holding.getHoldingId());
        assertThat(found).isEmpty();
    }
}