package com.wealthpro.orderexecution.repository;

import com.wealthpro.orderexecution.entities.ExecutionFill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link ExecutionFill} entities.
 *
 * @author WealthPro Team
 */
@Repository
public interface ExecutionFillRepository extends JpaRepository<ExecutionFill, Long> {

    List<ExecutionFill> findByOrder_OrderId(Long orderId);
}
