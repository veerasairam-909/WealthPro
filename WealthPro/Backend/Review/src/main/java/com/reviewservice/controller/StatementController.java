package com.reviewservice.controller;

import com.reviewservice.dto.request.StatementRequest;
import com.reviewservice.dto.response.StatementResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface StatementController {

    ResponseEntity<StatementResponse> createStatement(@Valid @RequestBody StatementRequest request,
                                                      String username, String roles, Long clientId);

    ResponseEntity<StatementResponse> getStatementById(@PathVariable Long statementId,
                                                       String username, String roles, Long clientId);

    ResponseEntity<List<StatementResponse>> getAllStatements(String username, String roles, Long clientId);

    ResponseEntity<List<StatementResponse>> getStatementsByAccountId(@PathVariable Long accountId,
                                                                     String username, String roles, Long clientId);

    ResponseEntity<StatementResponse> updateStatement(@PathVariable Long statementId,
                                                      @Valid @RequestBody StatementRequest request,
                                                      String username, String roles, Long clientId);

    ResponseEntity<Void> deleteStatement(@PathVariable Long statementId,
                                         String username, String roles, Long clientId);
}
