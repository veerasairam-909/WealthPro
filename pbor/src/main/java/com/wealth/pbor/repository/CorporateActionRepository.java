package com.wealth.pbor.repository;

import com.wealth.pbor.entity.CorporateAction;
import com.wealth.pbor.enums.CAType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CorporateActionRepository extends JpaRepository<CorporateAction, Long> {

    List<CorporateAction> findBySecurityId(Long securityId);

    List<CorporateAction> findByCaType(CAType caType);

    List<CorporateAction> findByRecordDateBetween(LocalDate from, LocalDate to);
}