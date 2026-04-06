package com.wealth.pbor.repository;

import com.wealth.pbor.entity.CashLedger;
import com.wealth.pbor.enums.TxnType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CashLedgerRepository extends JpaRepository<CashLedger, Long> {

    List<CashLedger> findByAccountAccountId(Long accountId);

    List<CashLedger> findByAccountAccountIdAndTxnType(Long accountId, TxnType txnType);

    List<CashLedger> findByAccountAccountIdAndTxnDateBetween(Long accountId, LocalDate from, LocalDate to);
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM CashLedger c WHERE c.account.accountId = :accountId")
    BigDecimal sumAmountByAccountId(@Param("accountId") Long accountId);
}