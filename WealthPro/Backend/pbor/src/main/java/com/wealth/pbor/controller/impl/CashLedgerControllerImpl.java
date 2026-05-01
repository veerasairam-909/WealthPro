package com.wealth.pbor.controller.impl;

import com.wealth.pbor.dto.request.CashLedgerRequest;
import com.wealth.pbor.dto.response.CashLedgerResponse;
import com.wealth.pbor.enums.TxnType;
import com.wealth.pbor.security.AuthContext;
import com.wealth.pbor.security.OwnershipGuard;
import com.wealth.pbor.service.CashLedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cash-ledger")
@RequiredArgsConstructor
public class CashLedgerControllerImpl {

    private final CashLedgerService cashLedgerService;
    private final OwnershipGuard ownershipGuard;

    @PostMapping
    public ResponseEntity<CashLedgerResponse> createCashLedgerEntry(@Valid @RequestBody CashLedgerRequest request) {
        // Writes staff-only at gateway.
        CashLedgerResponse response = cashLedgerService.createCashLedgerEntry(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{ledgerId}")
    public ResponseEntity<CashLedgerResponse> getCashLedgerEntryById(
            @PathVariable Long ledgerId,
            @RequestHeader(value = AuthContext.HDR_ROLES,     required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        CashLedgerResponse response = cashLedgerService.getCashLedgerEntryById(ledgerId);
        ownershipGuard.checkAccount(new AuthContext(null, roles, authClientId), response.getAccountId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CashLedgerResponse>> getAllCashLedgerEntries(
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles) {
        if (new AuthContext(null, roles, null).isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Clients cannot list all cash ledger entries.");
        }
        return ResponseEntity.ok(cashLedgerService.getAllCashLedgerEntries());
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<CashLedgerResponse>> getCashLedgerEntriesByAccountId(
            @PathVariable Long accountId,
            @RequestHeader(value = AuthContext.HDR_ROLES,     required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        ownershipGuard.checkAccount(new AuthContext(null, roles, authClientId), accountId);
        return ResponseEntity.ok(cashLedgerService.getCashLedgerEntriesByAccountId(accountId));
    }

    @GetMapping("/account/{accountId}/txn-type/{txnType}")
    public ResponseEntity<List<CashLedgerResponse>> getCashLedgerEntriesByAccountIdAndTxnType(
            @PathVariable Long accountId,
            @PathVariable TxnType txnType,
            @RequestHeader(value = AuthContext.HDR_ROLES,     required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        ownershipGuard.checkAccount(new AuthContext(null, roles, authClientId), accountId);
        return ResponseEntity.ok(cashLedgerService.getCashLedgerEntriesByAccountIdAndTxnType(accountId, txnType));
    }

    @GetMapping("/account/{accountId}/date-range")
    public ResponseEntity<List<CashLedgerResponse>> getCashLedgerEntriesByAccountIdAndDateRange(
            @PathVariable Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestHeader(value = AuthContext.HDR_ROLES,     required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        ownershipGuard.checkAccount(new AuthContext(null, roles, authClientId), accountId);
        return ResponseEntity.ok(cashLedgerService.getCashLedgerEntriesByAccountIdAndDateRange(accountId, from, to));
    }

    @GetMapping("/account/{accountId}/balance")
    public ResponseEntity<BigDecimal> getBalanceByAccountId(
            @PathVariable Long accountId,
            @RequestHeader(value = AuthContext.HDR_ROLES,     required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {
        ownershipGuard.checkAccount(new AuthContext(null, roles, authClientId), accountId);
        return ResponseEntity.ok(cashLedgerService.getBalanceByAccountId(accountId));
    }

    @PutMapping("/{ledgerId}")
    public ResponseEntity<CashLedgerResponse> updateCashLedgerEntry(@PathVariable Long ledgerId,
                                                                    @Valid @RequestBody CashLedgerRequest request) {
        return ResponseEntity.ok(cashLedgerService.updateCashLedgerEntry(ledgerId, request));
    }

    @DeleteMapping("/{ledgerId}")
    public ResponseEntity<String> deleteCashLedgerEntry(@PathVariable Long ledgerId) {
        cashLedgerService.deleteCashLedgerEntry(ledgerId);
        return ResponseEntity.ok("Cash ledger entry deleted successfully.");
    }
}
