package com.wealth.pbor.entity;

import com.wealth.pbor.enums.TxnType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cash_ledger")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CashLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ledger_id")
    private Long ledgerId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    @Enumerated(EnumType.STRING)
    @Column(name = "txn_type", nullable = false, length = 20)
    private TxnType txnType;
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;
    @Column(name = "currency", nullable = false, length = 10)
    private String currency;
    @Column(name = "txn_date", nullable = false)
    private LocalDate txnDate;
    @Column(name = "narrative", length = 500)
    private String narrative;
}