package com.wealth.goalsadvisory.repository;

import com.wealth.goalsadvisory.entity.Recommendation;
import com.wealth.goalsadvisory.enums.RecommendationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    List<Recommendation> findByClientId(Long clientId);

    List<Recommendation> findByClientIdAndStatus(Long clientId, RecommendationStatus status);

    List<Recommendation> findByModelPortfolio_ModelId(Long modelId);
}