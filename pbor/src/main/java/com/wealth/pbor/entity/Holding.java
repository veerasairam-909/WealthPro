package com.wealth.pbor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "holding")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_id")
    private Long holdingId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    @Column(name = "security_id", nullable = false)
    private Long securityId;
    @Column(name = "quantity", nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;
    @Column(name = "avg_cost", nullable = false, precision = 18, scale = 4)
    private BigDecimal avgCost;
    @Column(name = "valuation_currency", nullable = false, length = 10)
    private String valuationCurrency;
    @Column(name = "last_valuation_date")
    private LocalDate lastValuationDate;
}