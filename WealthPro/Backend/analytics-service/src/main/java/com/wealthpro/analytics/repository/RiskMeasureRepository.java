package com.wealthpro.analytics.repository;

import com.wealthpro.analytics.entities.RiskMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/** JPA repository for {@link RiskMeasure} entities. */
@Repository
public interface RiskMeasureRepository extends JpaRepository<RiskMeasure, Long> {
    List<RiskMeasure> findByAccountId(Long accountId);
}
