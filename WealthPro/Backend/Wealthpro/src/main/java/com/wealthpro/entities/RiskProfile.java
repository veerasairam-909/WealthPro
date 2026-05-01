package com.wealthpro.entities;

import com.wealthpro.enums.RiskClass;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "RiskProfile")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "client")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RiskProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RiskID")
    @EqualsAndHashCode.Include
    private Long riskId;


    // @OneToOne — one client has exactly one risk profile.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    @Column(name = "QuestionnaireJSON", nullable = false, columnDefinition = "TEXT")
    private String questionnaireJSON;

    @Column(name = "RiskScore", nullable = false, precision = 5, scale = 2)
    private BigDecimal riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "RiskClass", nullable = false, length = 15)
    private RiskClass riskClass;

    @Column(name = "AssessedDate", nullable = false)
    private LocalDate assessedDate;
}