package com.wealth.pbor.controller;

import com.wealth.pbor.dto.request.CashLedgerRequest;
import com.wealth.pbor.dto.response.CashLedgerResponse;
import com.wealth.pbor.enums.TxnType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface CashLedgerController {

    ResponseEntity<CashLedgerResponse> createEntry(CashLedgerRequest request);

    ResponseEntity<CashLedgerResponse> getEntryById(Long ledgerId);

    ResponseEntity<List<CashLedgerResponse>> getEntriesByAccountId(Long accountId);

    ResponseEntity<List<CashLedgerResponse>> getEntriesByAccountIdAndTxnType(
            Long accountId, TxnType txnType);

    ResponseEntity<List<CashLedgerResponse>> getEntriesByDateRange(
            Long accountId, LocalDate from, LocalDate to);

    ResponseEntity<Void> deleteEntry(Long ledgerId);
}