package com.wealth.pbor.controller;

import com.wealth.pbor.dto.request.CashLedgerRequest;
import com.wealth.pbor.dto.response.CashLedgerResponse;
import com.wealth.pbor.enums.TxnType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CashLedgerController {

    ResponseEntity<CashLedgerResponse> createCashLedgerEntry(CashLedgerRequest request);

    ResponseEntity<CashLedgerResponse> getCashLedgerEntryById(Long ledgerId);

    ResponseEntity<List<CashLedgerResponse>> getAllCashLedgerEntries();

    ResponseEntity<List<CashLedgerResponse>> getCashLedgerEntriesByAccountId(Long accountId);

    ResponseEntity<List<CashLedgerResponse>> getCashLedgerEntriesByAccountIdAndTxnType(Long accountId, TxnType txnType);

    ResponseEntity<List<CashLedgerResponse>> getCashLedgerEntriesByAccountIdAndDateRange(Long accountId, LocalDate from, LocalDate to);

    ResponseEntity<BigDecimal> getBalanceByAccountId(Long accountId);

    ResponseEntity<CashLedgerResponse> updateCashLedgerEntry(Long ledgerId, CashLedgerRequest request);

    ResponseEntity<String> deleteCashLedgerEntry(Long ledgerId);
}