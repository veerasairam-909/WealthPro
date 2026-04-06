package com.wealthpro.orderexecution.repository;

import com.wealthpro.orderexecution.entities.PreTradeCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link PreTradeCheck} entities.
 *
 * @author WealthPro Team
 */
@Repository
public interface PreTradeCheckRepository extends JpaRepository<PreTradeCheck, Long> {

    List<PreTradeCheck> findByOrder_OrderId(Long orderId);
}
