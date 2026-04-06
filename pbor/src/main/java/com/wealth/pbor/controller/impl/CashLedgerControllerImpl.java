package com.wealth.pbor.controller.impl;

import com.wealth.pbor.dto.request.CashLedgerRequest;
import com.wealth.pbor.dto.response.CashLedgerResponse;
import com.wealth.pbor.enums.TxnType;
import com.wealth.pbor.service.CashLedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cash-ledger")
@RequiredArgsConstructor
public class CashLedgerControllerImpl {

    private final CashLedgerService cashLedgerService;

    @PostMapping
    public ResponseEntity<CashLedgerResponse> createCashLedgerEntry(@Valid @RequestBody CashLedgerRequest request) {
        CashLedgerResponse response = cashLedgerService.createCashLedgerEntry(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{ledgerId}")
    public ResponseEntity<CashLedgerResponse> getCashLedgerEntryById(@PathVariable Long ledgerId) {
        CashLedgerResponse response = cashLedgerService.getCashLedgerEntryById(ledgerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CashLedgerResponse>> getAllCashLedgerEntries() {
        List<CashLedgerResponse> responseList = cashLedgerService.getAllCashLedgerEntries();
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<CashLedgerResponse>> getCashLedgerEntriesByAccountId(@PathVariable Long accountId) {
        List<CashLedgerResponse> responseList = cashLedgerService.getCashLedgerEntriesByAccountId(accountId);
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/account/{accountId}/txn-type/{txnType}")
    public ResponseEntity<List<CashLedgerResponse>> getCashLedgerEntriesByAccountIdAndTxnType(
            @PathVariable Long accountId,
            @PathVariable TxnType txnType) {
        List<CashLedgerResponse> responseList = cashLedgerService.getCashLedgerEntriesByAccountIdAndTxnType(accountId, txnType);
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/account/{accountId}/date-range")
    public ResponseEntity<List<CashLedgerResponse>> getCashLedgerEntriesByAccountIdAndDateRange(
            @PathVariable Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<CashLedgerResponse> responseList = cashLedgerService.getCashLedgerEntriesByAccountIdAndDateRange(accountId, from, to);
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/account/{accountId}/balance")
    public ResponseEntity<BigDecimal> getBalanceByAccountId(@PathVariable Long accountId) {
        BigDecimal balance = cashLedgerService.getBalanceByAccountId(accountId);
        return ResponseEntity.ok(balance);
    }

    @PutMapping("/{ledgerId}")
    public ResponseEntity<CashLedgerResponse> updateCashLedgerEntry(@PathVariable Long ledgerId,
                                                                    @Valid @RequestBody CashLedgerRequest request) {
        CashLedgerResponse response = cashLedgerService.updateCashLedgerEntry(ledgerId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{ledgerId}")
    public ResponseEntity<String> deleteCashLedgerEntry(@PathVariable Long ledgerId) {
        cashLedgerService.deleteCashLedgerEntry(ledgerId);
        return ResponseEntity.ok("Cash ledger entry deleted successfully.");
    }
}
