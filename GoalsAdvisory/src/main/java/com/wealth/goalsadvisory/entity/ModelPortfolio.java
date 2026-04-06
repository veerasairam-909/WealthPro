package com.wealth.goalsadvisory.entity;

import com.wealth.goalsadvisory.enums.ModelPortfolioStatus;
import com.wealth.goalsadvisory.enums.RiskClass;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "model_portfolios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModelPortfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_id")
    private Long modelId;
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_class", nullable = false, length = 20)
    private RiskClass riskClass;


    @Column(name = "weights_json", nullable = false, columnDefinition = "TEXT")
    private String weightsJson;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ModelPortfolioStatus status = ModelPortfolioStatus.ACTIVE;

    @OneToMany(mappedBy = "modelPortfolio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Recommendation> recommendations = new ArrayList<>();


}