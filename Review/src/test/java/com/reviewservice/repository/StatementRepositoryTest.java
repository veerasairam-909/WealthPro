package com.reviewservice.repository;

import com.reviewservice.entity.Statement;
import com.reviewservice.enums.PeriodType;
import com.reviewservice.enums.StatementStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class StatementRepositoryTest {

    @Autowired
    private StatementRepository statementRepository;

    private Statement statement;

    @BeforeEach
    void setUp() {
        statementRepository.deleteAll();

        statement = Statement.builder()
                .accountId(201L)
                .periodStart(LocalDate.of(2024, 1, 1))
                .periodEnd(LocalDate.of(2024, 1, 31))
                .periodType(PeriodType.MONTHLY)
                .generatedDate(LocalDate.of(2024, 2, 1))
                .summaryJson("{\"totalValue\": \"100000\"}")
                .status(StatementStatus.GENERATED)
                .build();

        statementRepository.save(statement);
    }

    @Test
    @DisplayName("Should save and retrieve a statement by ID")
    void testSaveAndFindById() {
        Optional<Statement> found = statementRepository.findById(statement.getStatementId());
        assertTrue(found.isPresent());
        assertEquals(201L, found.get().getAccountId());
        assertEquals(StatementStatus.GENERATED, found.get().getStatus());
    }

    @Test
    @DisplayName("Should find all statements by account ID")
    void testFindByAccountId() {
        List<Statement> statements = statementRepository.findByAccountId(201L);
        assertFalse(statements.isEmpty());
        assertEquals(1, statements.size());
        assertEquals(201L, statements.get(0).getAccountId());
    }

    @Test
    @DisplayName("Should find statements by account ID and status")
    void testFindByAccountIdAndStatus() {
        List<Statement> statements = statementRepository.findByAccountIdAndStatus(201L, StatementStatus.GENERATED);
        assertFalse(statements.isEmpty());
        assertEquals(StatementStatus.GENERATED, statements.get(0).getStatus());
    }

    @Test
    @DisplayName("Should return true if statement exists for account")
    void testExistsByAccountId() {
        assertTrue(statementRepository.existsByAccountId(201L));
        assertFalse(statementRepository.existsByAccountId(999L));
    }

    @Test
    @DisplayName("Should delete a statement by ID")
    void testDeleteById() {
        statementRepository.deleteById(statement.getStatementId());
        Optional<Statement> found = statementRepository.findById(statement.getStatementId());
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should return all statements")
    void testFindAll() {
        List<Statement> all = statementRepository.findAll();
        assertNotNull(all);
        assertEquals(1, all.size());
    }
}
