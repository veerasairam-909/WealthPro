package com.wealthpro.orderexecution.repository;

import com.wealthpro.orderexecution.entities.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Allocation} entities.
 *
 * @author WealthPro Team
 */
@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {

    List<Allocation> findByOrder_OrderId(Long orderId);

    List<Allocation> findByAccountId(Long accountId);
}
