package com.wealthpro.entities;

import com.wealthpro.enums.RuleStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SuitabilityRule")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SuitabilityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RuleID")
    @EqualsAndHashCode.Include
    private Long ruleId;

    @Column(name = "Description", nullable = false, length = 250)
    private String description;

    @Column(name = "Expression", nullable = false, columnDefinition = "TEXT")
    private String expression;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 10)
    private RuleStatus status;
}