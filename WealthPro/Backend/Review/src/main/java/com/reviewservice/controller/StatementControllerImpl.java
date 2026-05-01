package com.reviewservice.controller;

import com.reviewservice.dto.request.StatementRequest;
import com.reviewservice.dto.response.StatementResponse;
import com.reviewservice.security.AuthContext;
import com.reviewservice.security.OwnershipGuard;
import com.reviewservice.service.StatementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/statements")
public class StatementControllerImpl implements StatementController {

    private final StatementService statementService;
    private final OwnershipGuard ownershipGuard;

    public StatementControllerImpl(StatementService statementService, OwnershipGuard ownershipGuard) {
        this.statementService = statementService;
        this.ownershipGuard = ownershipGuard;
    }

    @Override
    @PostMapping
    public ResponseEntity<StatementResponse> createStatement(
            @Valid @RequestBody StatementRequest request,
            @RequestHeader(value = AuthContext.HDR_USERNAME, required = false) String username,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long clientId) {
        AuthContext ctx = new AuthContext(username, roles, clientId);
        if (ctx.isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clients cannot create statements.");
        }
        StatementResponse response = statementService.createStatement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping("/{statementId}")
    public ResponseEntity<StatementResponse> getStatementById(
            @PathVariable Long statementId,
            @RequestHeader(value = AuthContext.HDR_USERNAME, required = false) String username,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long clientId) {
        AuthContext ctx = new AuthContext(username, roles, clientId);
        StatementResponse response = statementService.getStatementById(statementId);
        ownershipGuard.checkAccount(ctx, response.getAccountId());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<StatementResponse>> getAllStatements(
            @RequestHeader(value = AuthContext.HDR_USERNAME, required = false) String username,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long clientId) {
        AuthContext ctx = new AuthContext(username, roles, clientId);
        if (ctx.isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clients cannot list all statements.");
        }
        List<StatementResponse> responses = statementService.getAllStatements();
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<StatementResponse>> getStatementsByAccountId(
            @PathVariable Long accountId,
            @RequestHeader(value = AuthContext.HDR_USERNAME, required = false) String username,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long clientId) {
        AuthContext ctx = new AuthContext(username, roles, clientId);
        ownershipGuard.checkAccount(ctx, accountId);
        List<StatementResponse> responses = statementService.getStatementsByAccountId(accountId);
        return ResponseEntity.ok(responses);
    }

    @Override
    @PutMapping("/{statementId}")
    public ResponseEntity<StatementResponse> updateStatement(
            @PathVariable Long statementId,
            @Valid @RequestBody StatementRequest request,
            @RequestHeader(value = AuthContext.HDR_USERNAME, required = false) String username,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long clientId) {
        AuthContext ctx = new AuthContext(username, roles, clientId);
        if (ctx.isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clients cannot update statements.");
        }
        StatementResponse response = statementService.updateStatement(statementId, request);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{statementId}")
    public ResponseEntity<Void> deleteStatement(
            @PathVariable Long statementId,
            @RequestHeader(value = AuthContext.HDR_USERNAME, required = false) String username,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long clientId) {
        AuthContext ctx = new AuthContext(username, roles, clientId);
        if (ctx.isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clients cannot delete statements.");
        }
        statementService.deleteStatement(statementId);
        return ResponseEntity.noContent().build();
    }
}
