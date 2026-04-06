package com.wealth.pbor.repository;

import com.wealth.pbor.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    List<Holding> findByAccountAccountId(Long accountId);

    List<Holding> findBySecurityId(Long securityId);
    Optional<Holding> findByAccountAccountIdAndSecurityId(Long accountId, Long securityId);
    boolean existsByAccountAccountIdAndSecurityId(Long accountId, Long securityId);
}