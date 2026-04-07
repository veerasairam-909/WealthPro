package com.wealthpro.analytics.repository;

import com.wealthpro.analytics.entities.ComplianceBreach;
import com.wealthpro.analytics.enums.BreachStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/** JPA repository for {@link ComplianceBreach} entities. */
@Repository
public interface ComplianceBreachRepository extends JpaRepository<ComplianceBreach, Long> {
    List<ComplianceBreach> findByAccountId(Long accountId);
    List<ComplianceBreach> findByAccountIdAndStatus(Long accountId, BreachStatus status);
}
