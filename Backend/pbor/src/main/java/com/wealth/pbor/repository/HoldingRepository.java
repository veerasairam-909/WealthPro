package com.wealth.pbor.repository;

import com.wealth.pbor.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    List<Holding> findByAccountAccountId(Long accountId);

    List<Holding> findBySecurityId(Long securityId);

    boolean existsByAccountAccountIdAndSecurityId(Long accountId, Long securityId);
}