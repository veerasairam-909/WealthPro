package com.wealth.pbor.repositorytest;

import com.wealth.pbor.entity.Account;
import com.wealth.pbor.enums.AccountStatus;
import com.wealth.pbor.enums.AccountType;
import com.wealth.pbor.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    private Account account;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        account = new Account();
        account.setClientId(1L);
        account.setAccountType(AccountType.INDIVIDUAL);
        account.setBaseCurrency("INR");
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
    }

    @Test
    void testSaveAccount() {
        Account newAccount = new Account();
        newAccount.setClientId(2L);
        newAccount.setAccountType(AccountType.JOINT);
        newAccount.setBaseCurrency("USD");
        newAccount.setStatus(AccountStatus.ACTIVE);
        Account saved = accountRepository.save(newAccount);
        assertThat(saved.getAccountId()).isNotNull();
        assertThat(saved.getClientId()).isEqualTo(2L);
    }

    @Test
    void testFindById() {
        Optional<Account> found = accountRepository.findById(account.getAccountId());
        assertThat(found).isPresent();
        assertThat(found.get().getClientId()).isEqualTo(1L);
    }

    @Test
    void testFindByClientId() {
        List<Account> accounts = accountRepository.findByClientId(1L);
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getAccountType()).isEqualTo(AccountType.INDIVIDUAL);
    }

    @Test
    void testFindByStatus() {
        List<Account> accounts = accountRepository.findByStatus(AccountStatus.ACTIVE);
        assertThat(accounts).isNotEmpty();
        assertThat(accounts.get(0).getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void testExistsByClientIdAndAccountType() {
        boolean exists = accountRepository.existsByClientIdAndAccountType(1L, AccountType.INDIVIDUAL);
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByClientIdAndAccountType_NotFound() {
        boolean exists = accountRepository.existsByClientIdAndAccountType(1L, AccountType.TRUST);
        assertThat(exists).isFalse();
    }

    @Test
    void testDeleteAccount() {
        accountRepository.delete(account);
        Optional<Account> found = accountRepository.findById(account.getAccountId());
        assertThat(found).isEmpty();
    }

    @Test
    void testFindAll() {
        List<Account> accounts = accountRepository.findAll();
        assertThat(accounts).hasSize(1);
    }
}