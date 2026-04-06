package com.wealth.goalsadvisory.repository;

import com.wealth.goalsadvisory.entity.ModelPortfolio;
import com.wealth.goalsadvisory.enums.ModelPortfolioStatus;
import com.wealth.goalsadvisory.enums.RiskClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelPortfolioRepository extends JpaRepository<ModelPortfolio, Long> {

    List<ModelPortfolio> findByRiskClass(RiskClass riskClass);

    List<ModelPortfolio> findByStatus(ModelPortfolioStatus status);

    List<ModelPortfolio> findByRiskClassAndStatus(RiskClass riskClass, ModelPortfolioStatus status);

    Optional<ModelPortfolio> findByName(String name);
    boolean existsByName(String name);
}