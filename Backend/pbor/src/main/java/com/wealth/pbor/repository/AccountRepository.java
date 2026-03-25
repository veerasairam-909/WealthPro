package com.wealth.pbor.repository;

import com.wealth.pbor.entity.Account;
import com.wealth.pbor.enums.AccountStatus;
import com.wealth.pbor.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByClientId(Long clientId);

    List<Account> findByStatus(AccountStatus status);

   // List<Account> findByAccountType(AccountType accountType);

    //List<Account> findByClientIdAndStatus(Long clientId, AccountStatus status);

    boolean existsByClientIdAndAccountType(Long clientId, AccountType accountType);

    //Optional<Account> findByClientIdAndAccountType(Long clientId, AccountType accountType);
}