package com.wealth.pbor.service.impl;

import com.wealth.pbor.dto.request.CashLedgerRequest;
import com.wealth.pbor.dto.response.CashLedgerResponse;
import com.wealth.pbor.entity.Account;
import com.wealth.pbor.entity.CashLedger;
import com.wealth.pbor.enums.TxnType;
import com.wealth.pbor.exception.BadRequestException;
import com.wealth.pbor.exception.ResourceNotFoundException;
import com.wealth.pbor.repository.AccountRepository;
import com.wealth.pbor.repository.CashLedgerRepository;
import com.wealth.pbor.service.CashLedgerService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CashLedgerServiceImpl implements CashLedgerService {

    private final CashLedgerRepository cashLedgerRepository;
    private final AccountRepository accountRepository;
    private final ModelMapper mapper;

    @Override
    public CashLedgerResponse createCashLedgerEntry(CashLedgerRequest request) {
        Optional<Account> optionalAccount = accountRepository.findById(request.getAccountId());
        if (optionalAccount.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with id: " + request.getAccountId());
        }
        if (request.getTxnDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be a future date.");
        }
        CashLedger cashLedger = new CashLedger();
        cashLedger.setAccount(optionalAccount.get());
        cashLedger.setTxnType(request.getTxnType());
        cashLedger.setAmount(request.getAmount());
        cashLedger.setCurrency(request.getCurrency());
        cashLedger.setTxnDate(request.getTxnDate());
        cashLedger.setNarrative(request.getNarrative());
        CashLedger saved = cashLedgerRepository.save(cashLedger);
        return mapper.map(saved, CashLedgerResponse.class);
    }

    @Override
    public CashLedgerResponse getCashLedgerEntryById(Long ledgerId) {
        Optional<CashLedger> optional = cashLedgerRepository.findById(ledgerId);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Cash ledger entry not found with id: " + ledgerId);
        }
        return mapper.map(optional.get(), CashLedgerResponse.class);
    }

    @Override
    public List<CashLedgerResponse> getAllCashLedgerEntries() {
        List<CashLedger> entries = cashLedgerRepository.findAll();
        List<CashLedgerResponse> responseList = new ArrayList<>();
        for (CashLedger entry : entries) {
            responseList.add(mapper.map(entry, CashLedgerResponse.class));
        }
        return responseList;
    }

    @Override
    public List<CashLedgerResponse> getCashLedgerEntriesByAccountId(Long accountId) {
        Optional<Account> optional = accountRepository.findById(accountId);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with id: " + accountId);
        }
        List<CashLedger> entries = cashLedgerRepository.findByAccountAccountId(accountId);
        List<CashLedgerResponse> responseList = new ArrayList<>();
        for (CashLedger entry : entries) {
            responseList.add(mapper.map(entry, CashLedgerResponse.class));
        }
        return responseList;
    }

    @Override
    public List<CashLedgerResponse> getCashLedgerEntriesByAccountIdAndTxnType(Long accountId, TxnType txnType) {
        Optional<Account> optional = accountRepository.findById(accountId);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with id: " + accountId);
        }
        List<CashLedger> entries = cashLedgerRepository.findByAccountAccountIdAndTxnType(accountId, txnType);
        List<CashLedgerResponse> responseList = new ArrayList<>();
        for (CashLedger entry : entries) {
            responseList.add(mapper.map(entry, CashLedgerResponse.class));
        }
        return responseList;
    }

    @Override
    public List<CashLedgerResponse> getCashLedgerEntriesByAccountIdAndDateRange(Long accountId, LocalDate from, LocalDate to) {
        Optional<Account> optional = accountRepository.findById(accountId);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with id: " + accountId);
        }
        if (from.isAfter(to)) {
            throw new BadRequestException("From date cannot be after to date.");
        }
        List<CashLedger> entries = cashLedgerRepository.findByAccountAccountIdAndTxnDateBetween(accountId, from, to);
        List<CashLedgerResponse> responseList = new ArrayList<>();
        for (CashLedger entry : entries) {
            responseList.add(mapper.map(entry, CashLedgerResponse.class));
        }
        return responseList;
    }

    @Override
    public CashLedgerResponse updateCashLedgerEntry(Long ledgerId, CashLedgerRequest request) {
        Optional<CashLedger> optionalEntry = cashLedgerRepository.findById(ledgerId);
        if (optionalEntry.isEmpty()) {
            throw new ResourceNotFoundException("Cash ledger entry not found with id: " + ledgerId);
        }
        Optional<Account> optionalAccount = accountRepository.findById(request.getAccountId());
        if (optionalAccount.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with id: " + request.getAccountId());
        }
        if (request.getTxnDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be a future date.");
        }
        CashLedger cashLedger = optionalEntry.get();
        cashLedger.setAccount(optionalAccount.get());
        cashLedger.setTxnType(request.getTxnType());
        cashLedger.setAmount(request.getAmount());
        cashLedger.setCurrency(request.getCurrency());
        cashLedger.setTxnDate(request.getTxnDate());
        cashLedger.setNarrative(request.getNarrative());
        CashLedger updated = cashLedgerRepository.save(cashLedger);
        return mapper.map(updated, CashLedgerResponse.class);
    }

    @Override
    public void deleteCashLedgerEntry(Long ledgerId) {
        Optional<CashLedger> optional = cashLedgerRepository.findById(ledgerId);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Cash ledger entry not found with id: " + ledgerId);
        }
        cashLedgerRepository.delete(optional.get());
    }
}