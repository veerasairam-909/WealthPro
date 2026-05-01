package com.reviewservice.repository;

import com.reviewservice.entity.Statement;
import com.reviewservice.enums.StatementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatementRepository extends JpaRepository<Statement, Long> {

    // Find all statements for a specific account
    List<Statement> findByAccountId(Long accountId);

    // Find all statements for an account filtered by status
    List<Statement> findByAccountIdAndStatus(Long accountId, StatementStatus status);

    // Check if a statement exists for an account
    boolean existsByAccountId(Long accountId);
}
