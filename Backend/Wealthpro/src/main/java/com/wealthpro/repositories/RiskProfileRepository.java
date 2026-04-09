package com.wealthpro.repositories;

import com.wealthpro.entities.RiskProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiskProfileRepository extends JpaRepository<RiskProfile, Long> {

    // Find risk profile by client ID
    // Used in: getRiskProfileByClientId, updateRiskProfile, deleteRiskProfile
    Optional<RiskProfile> findByClientClientId(Long clientId);

    // Check if risk profile already exists for a client
    // Used in: createRiskProfile to prevent duplicates
    boolean existsByClientClientId(Long clientId);
}