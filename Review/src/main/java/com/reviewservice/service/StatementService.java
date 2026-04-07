package com.reviewservice.service;

import com.reviewservice.dto.request.StatementRequest;
import com.reviewservice.dto.response.StatementResponse;

import java.util.List;

public interface StatementService {

    StatementResponse createStatement(StatementRequest request);

    StatementResponse getStatementById(Long statementId);

    List<StatementResponse> getAllStatements();

    List<StatementResponse> getStatementsByAccountId(Long accountId);

    StatementResponse updateStatement(Long statementId, StatementRequest request);

    void deleteStatement(Long statementId);
}
