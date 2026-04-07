package com.reviewservice.service;

import com.reviewservice.dto.request.StatementRequest;
import com.reviewservice.dto.response.StatementResponse;
import com.reviewservice.entity.Statement;
import com.reviewservice.enums.PeriodType;
import com.reviewservice.enums.StatementStatus;
import com.reviewservice.exception.ResourceNotFoundException;
import com.reviewservice.repository.StatementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatementServiceTest {

    @Mock
    private StatementRepository statementRepository;

    @InjectMocks
    private StatementServiceImpl statementService;

    private Statement statement;
    private StatementRequest statementRequest;

    @BeforeEach
    void setUp() {
        statement = Statement.builder()
                .statementId(1L)
                .accountId(201L)
                .periodStart(LocalDate.of(2024, 1, 1))
                .periodEnd(LocalDate.of(2024, 1, 31))
                .periodType(PeriodType.MONTHLY)
                .generatedDate(LocalDate.of(2024, 2, 1))
                .summaryJson("{\"totalValue\": \"100000\"}")
                .status(StatementStatus.GENERATED)
                .build();

        statementRequest = StatementRequest.builder()
                .accountId(201L)
                .periodStart(LocalDate.of(2024, 1, 1))
                .periodEnd(LocalDate.of(2024, 1, 31))
                .periodType(PeriodType.MONTHLY)
                .generatedDate(LocalDate.of(2024, 2, 1))
                .summaryJson("{\"totalValue\": \"100000\"}")
                .status(StatementStatus.GENERATED)
                .build();
    }

    @Test
    @DisplayName("Should create a statement successfully")
    void testCreateStatement() {
        when(statementRepository.save(any(Statement.class))).thenReturn(statement);

        StatementResponse result = statementService.createStatement(statementRequest);

        assertNotNull(result);
        assertEquals(201L, result.getAccountId());
        assertEquals(StatementStatus.GENERATED, result.getStatus());
        verify(statementRepository, times(1)).save(any(Statement.class));
    }

    @Test
    @DisplayName("Should return statement by ID")
    void testGetStatementById() {
        when(statementRepository.findById(1L)).thenReturn(Optional.of(statement));

        StatementResponse result = statementService.getStatementById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getStatementId());
        assertEquals(201L, result.getAccountId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when statement not found")
    void testGetStatementById_NotFound() {
        when(statementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> statementService.getStatementById(99L));
    }

    @Test
    @DisplayName("Should return all statements")
    void testGetAllStatements() {
        when(statementRepository.findAll()).thenReturn(List.of(statement));

        List<StatementResponse> result = statementService.getAllStatements();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(201L, result.get(0).getAccountId());
    }

    @Test
    @DisplayName("Should update statement successfully")
    void testUpdateStatement() {
        when(statementRepository.findById(1L)).thenReturn(Optional.of(statement));
        when(statementRepository.save(any(Statement.class))).thenReturn(statement);

        StatementResponse result = statementService.updateStatement(1L, statementRequest);

        assertNotNull(result);
        verify(statementRepository, times(1)).save(any(Statement.class));
    }

    @Test
    @DisplayName("Should delete statement successfully")
    void testDeleteStatement() {
        when(statementRepository.findById(1L)).thenReturn(Optional.of(statement));

        statementService.deleteStatement(1L);

        verify(statementRepository, times(1)).delete(statement);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException on delete when statement not found")
    void testDeleteStatement_NotFound() {
        when(statementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> statementService.deleteStatement(99L));
    }
}
