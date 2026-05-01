package com.wealthpro.repositories;

import com.wealthpro.entities.AmlFlag;
import com.wealthpro.enums.AmlFlagStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AmlFlagRepository extends JpaRepository<AmlFlag, Long> {

    List<AmlFlag> findByClientId(Long clientId);

    List<AmlFlag> findByStatus(AmlFlagStatus status);
}
