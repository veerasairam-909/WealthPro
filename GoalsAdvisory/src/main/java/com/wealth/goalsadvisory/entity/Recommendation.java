package com.wealth.goalsadvisory.entity;

import com.wealth.goalsadvisory.enums.RecommendationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reco_id")
    private Long recoId;
    @Column(name = "client_id", nullable = false)
    private Long clientId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = true)
    private Goal goal;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private ModelPortfolio modelPortfolio;
    @Column(name = "proposal_json", nullable = false, columnDefinition = "TEXT")
    private String proposalJson;

    @Column(name = "proposed_date", nullable = false)
    private LocalDate proposedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecommendationStatus status = RecommendationStatus.DRAFT;
}