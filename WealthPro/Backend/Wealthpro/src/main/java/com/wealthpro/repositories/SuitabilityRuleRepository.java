package com.wealthpro.repositories;

import com.wealthpro.entities.RiskProfile;
import com.wealthpro.entities.SuitabilityRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuitabilityRuleRepository extends JpaRepository<SuitabilityRule, Long> {
}
