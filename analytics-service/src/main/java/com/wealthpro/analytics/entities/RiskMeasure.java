package com.wealthpro.analytics.entities;

import com.wealthpro.analytics.enums.MeasureType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA entity storing risk measure calculations for a portfolio.
 *
 * @author WealthPro Team
 * @version 2.0
 */
@Entity
@Table(name = "risk_measures")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RiskMeasure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long measureId;

    @Column(nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeasureType measureType;

    @Column(nullable = false)
    private Double measureValue;

    private String description;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    @PrePersist
    protected void onCreate() {
        if (calculatedAt == null) calculatedAt = LocalDateTime.now();
    }
}
