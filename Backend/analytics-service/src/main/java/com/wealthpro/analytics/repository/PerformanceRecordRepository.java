package com.wealthpro.analytics.repository;

import com.wealthpro.analytics.entities.PerformanceRecord;
import com.wealthpro.analytics.enums.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/** JPA repository for {@link PerformanceRecord} entities. */
@Repository
public interface PerformanceRecordRepository extends JpaRepository<PerformanceRecord, Long> {
    List<PerformanceRecord> findByAccountId(Long accountId);
    List<PerformanceRecord> findByAccountIdAndPeriod(Long accountId, Period period);
}
