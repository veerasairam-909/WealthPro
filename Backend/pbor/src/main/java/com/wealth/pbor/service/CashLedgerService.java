package com.wealth.pbor.service;

import com.wealth.pbor.dto.request.CashLedgerRequest;
import com.wealth.pbor.dto.request.CashLedgerRequest;
import com.wealth.pbor.dto.response.CashLedgerResponse;
import com.wealth.pbor.dto.response.CashLedgerResponse;
import com.wealth.pbor.enums.TxnType;

import java.time.LocalDate;
import java.util.List;

public interface CashLedgerService {

    CashLedgerResponse createCashLedgerEntry(CashLedgerRequest requestDTO);

    CashLedgerResponse getCashLedgerEntryById(Long ledgerId);

    List<CashLedgerResponse> getAllCashLedgerEntries();

    List<CashLedgerResponse> getCashLedgerEntriesByAccountId(Long accountId);

    List<CashLedgerResponse> getCashLedgerEntriesByAccountIdAndTxnType(Long accountId, TxnType txnType);

    List<CashLedgerResponse> getCashLedgerEntriesByAccountIdAndDateRange(Long accountId, LocalDate from, LocalDate to);

    CashLedgerResponse updateCashLedgerEntry(Long ledgerId, CashLedgerRequest requestDTO);

    void deleteCashLedgerEntry(Long ledgerId);
}