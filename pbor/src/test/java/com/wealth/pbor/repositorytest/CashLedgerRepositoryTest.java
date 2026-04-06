package com.wealth.pbor.repositorytest;

import com.wealth.pbor.entity.Account;
import com.wealth.pbor.entity.CashLedger;
import com.wealth.pbor.enums.AccountStatus;
import com.wealth.pbor.enums.AccountType;
import com.wealth.pbor.enums.TxnType;
import com.wealth.pbor.repository.AccountRepository;
import com.wealth.pbor.repository.CashLedgerRepository;
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
class CashLedgerRepositoryTest {

    @Autowired
    private CashLedgerRepository cashLedgerRepository;

    @Autowired
    private AccountRepository accountRepository;

    private Account account;
    private CashLedger cashLedger;

    @BeforeEach
    void setUp() {
        cashLedgerRepository.deleteAll();
        accountRepository.deleteAll();

        account = new Account();
        account.setClientId(1L);
        account.setAccountType(AccountType.INDIVIDUAL);
        account.setBaseCurrency("INR");
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        cashLedger = new CashLedger();
        cashLedger.setAccount(account);
        cashLedger.setTxnType(TxnType.SUBSCRIPTION);
        cashLedger.setAmount(new BigDecimal("5000.00"));
        cashLedger.setCurrency("INR");
        cashLedger.setTxnDate(LocalDate.now().minusDays(1));
        cashLedger.setNarrative("Initial subscription");
        cashLedgerRepository.save(cashLedger);
    }

    @Test
    void testSaveCashLedger() {
        CashLedger newEntry = new CashLedger();
        newEntry.setAccount(account);
        newEntry.setTxnType(TxnType.DIVIDEND);
        newEntry.setAmount(new BigDecimal("200.00"));
        newEntry.setCurrency("INR");
        newEntry.setTxnDate(LocalDate.now().minusDays(1));
        newEntry.setNarrative("Dividend credit");
        CashLedger saved = cashLedgerRepository.save(newEntry);
        assertThat(saved.getLedgerId()).isNotNull();
    }

    @Test
    void testFindById() {
        Optional<CashLedger> found = cashLedgerRepository.findById(cashLedger.getLedgerId());
        assertThat(found).isPresent();
        assertThat(found.get().getTxnType()).isEqualTo(TxnType.SUBSCRIPTION);
    }

    @Test
    void testFindByAccountAccountId() {
        List<CashLedger> entries = cashLedgerRepository.findByAccountAccountId(account.getAccountId());
        assertThat(entries).hasSize(1);
    }

    @Test
    void testFindByAccountAccountIdAndTxnType() {
        List<CashLedger> entries = cashLedgerRepository.findByAccountAccountIdAndTxnType(
                account.getAccountId(), TxnType.SUBSCRIPTION);
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).getTxnType()).isEqualTo(TxnType.SUBSCRIPTION);
    }

    @Test
    void testFindByAccountAccountIdAndTxnDateBetween() {
        List<CashLedger> entries = cashLedgerRepository.findByAccountAccountIdAndTxnDateBetween(
                account.getAccountId(),
                LocalDate.now().minusDays(5),
                LocalDate.now());
        assertThat(entries).hasSize(1);
    }

    @Test
    void testFindByAccountAccountIdAndTxnDateBetween_NoResults() {
        List<CashLedger> entries = cashLedgerRepository.findByAccountAccountIdAndTxnDateBetween(
                account.getAccountId(),
                LocalDate.now().minusDays(30),
                LocalDate.now().minusDays(10));
        assertThat(entries).isEmpty();
    }

    @Test
    void testDeleteCashLedger() {
        cashLedgerRepository.delete(cashLedger);
        Optional<CashLedger> found = cashLedgerRepository.findById(cashLedger.getLedgerId());
        assertThat(found).isEmpty();
    }
}