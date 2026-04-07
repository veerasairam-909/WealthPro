package com.reviewservice.service;

import com.reviewservice.dto.request.StatementRequest;
import com.reviewservice.dto.response.StatementResponse;
import com.reviewservice.entity.Statement;
import com.reviewservice.exception.ResourceNotFoundException;
import com.reviewservice.repository.StatementRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatementServiceImpl implements StatementService {

    private final StatementRepository statementRepository;

    public StatementServiceImpl(StatementRepository statementRepository) {
        this.statementRepository = statementRepository;
    }

    // -------------------------------------------------------
    // Mapping Methods
    // -------------------------------------------------------

    private Statement mapToEntity(StatementRequest request) {
        Statement statement = new Statement();
        statement.setAccountId(request.getAccountId());
        statement.setPeriodStart(request.getPeriodStart());
        statement.setPeriodEnd(request.getPeriodEnd());
        statement.setPeriodType(request.getPeriodType());
        statement.setGeneratedDate(request.getGeneratedDate());
        statement.setSummaryJson(request.getSummaryJson());
        statement.setStatus(request.getStatus());
        return statement;
    }

    private StatementResponse mapToResponse(Statement statement) {
        StatementResponse response = new StatementResponse();
        response.setStatementId(statement.getStatementId());
        response.setAccountId(statement.getAccountId());
        response.setPeriodStart(statement.getPeriodStart());
        response.setPeriodEnd(statement.getPeriodEnd());
        response.setPeriodType(statement.getPeriodType());
        response.setGeneratedDate(statement.getGeneratedDate());
        response.setSummaryJson(statement.getSummaryJson());
        response.setStatus(statement.getStatus());
        return response;
    }

    // -------------------------------------------------------
    // Service Methods
    // -------------------------------------------------------

    @Override
    public StatementResponse createStatement(StatementRequest request) {
        Statement statement = mapToEntity(request);
        Statement saved = statementRepository.save(statement);
        return mapToResponse(saved);
    }

    @Override
    public StatementResponse getStatementById(Long statementId) {
        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new ResourceNotFoundException("Statement", statementId));
        return mapToResponse(statement);
    }

    @Override
    public List<StatementResponse> getAllStatements() {
        List<Statement> statements = statementRepository.findAll();
        List<StatementResponse> responses = new ArrayList<>();
        for (Statement statement : statements) {
            responses.add(mapToResponse(statement));
        }
        return responses;
    }

    @Override
    public List<StatementResponse> getStatementsByAccountId(Long accountId) {
        List<Statement> statements = statementRepository.findByAccountId(accountId);
        List<StatementResponse> responses = new ArrayList<>();
        for (Statement statement : statements) {
            responses.add(mapToResponse(statement));
        }
        return responses;
    }

    @Override
    public StatementResponse updateStatement(Long statementId, StatementRequest request) {
        Statement existing = statementRepository.findById(statementId)
                .orElseThrow(() -> new ResourceNotFoundException("Statement", statementId));

        existing.setAccountId(request.getAccountId());
        existing.setPeriodStart(request.getPeriodStart());
        existing.setPeriodEnd(request.getPeriodEnd());
        existing.setPeriodType(request.getPeriodType());
        existing.setGeneratedDate(request.getGeneratedDate());
        existing.setSummaryJson(request.getSummaryJson());
        existing.setStatus(request.getStatus());

        Statement updated = statementRepository.save(existing);
        return mapToResponse(updated);
    }

    @Override
    public void deleteStatement(Long statementId) {
        Statement existing = statementRepository.findById(statementId)
                .orElseThrow(() -> new ResourceNotFoundException("Statement", statementId));
        statementRepository.delete(existing);
    }
}
