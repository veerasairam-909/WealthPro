package com.wealthpro.analytics.entities;

import com.wealthpro.analytics.enums.Period;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity storing portfolio performance return records.
 *
 * @author WealthPro Team
 * @version 2.0
 */
@Entity
@Table(name = "performance_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PerformanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private Long portfolioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Period period;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Double returnPercentage;

    @Column(nullable = false)
    private Double benchmarkReturnPercentage;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    @PrePersist
    protected void onCreate() {
        if (calculatedAt == null) calculatedAt = LocalDateTime.now();
    }
}
