package com.wealthpro.analytics.entities;

import com.wealthpro.analytics.enums.BreachStatus;
import com.wealthpro.analytics.enums.Severity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a compliance breach detected during scanning.
 *
 * @author WealthPro Team
 * @version 2.0
 */
@Entity
@Table(name = "compliance_breaches")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplianceBreach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long breachId;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private String ruleViolated;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BreachStatus status;

    @Column(nullable = false)
    private LocalDateTime detectedAt;

    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        if (detectedAt == null) detectedAt = LocalDateTime.now();
        if (status == null) status = BreachStatus.OPEN;
    }
}
