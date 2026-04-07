package com.wealthpro.orderexecution.repository;

import com.wealthpro.orderexecution.entities.Order;
import com.wealthpro.orderexecution.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Order} entities.
 *
 * @author WealthPro Team
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByClientId(Long clientId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findBySecurityId(Long securityId);
}
