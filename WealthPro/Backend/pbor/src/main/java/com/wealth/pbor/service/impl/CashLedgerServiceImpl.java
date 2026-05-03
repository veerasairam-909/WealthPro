package com.wealth.pbor.service.impl;

import com.wealth.pbor.dto.request.CashLedgerRequest;
import com.wealth.pbor.dto.response.CashLedgerResponse;
import com.wealth.pbor.entity.Account;
import com.wealth.pbor.entity.CashLedger;
import com.wealth.pbor.entity.Holding;
import com.wealth.pbor.enums.TxnType;
import com.wealth.pbor.exception.BadRequestException;
import com.wealth.pbor.exception.ResourceNotFoundException;
import com.wealth.pbor.repository.AccountRepository;
import com.wealth.pbor.repository.CashLedgerRepository;
import com.wealth.pbor.repository.HoldingRepository;
import com.wealth.pbor.service.CashLedgerService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CashLedgerServiceImpl implements CashLedgerService {

    private final CashLedgerRepository cashLedgerRepository;
    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;
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

        Account account = optionalAccount.get();
        BigDecimal calculatedAmount;

        if (request.getTxnType() == TxnType.SUBSCRIPTION) {
            validateSecurityQuantityPrice(request);
            calculatedAmount = request.getQuantity().multiply(request.getPrice()).negate();
            processSubscription(account, request);

        } else if (request.getTxnType() == TxnType.REDEMPTION) {
            validateSecurityQuantityPrice(request);
            calculatedAmount = request.getQuantity().multiply(request.getPrice());
            processRedemption(account, request);

        } else if (request.getTxnType() == TxnType.DIVIDEND
                || request.getTxnType() == TxnType.CREDIT) {
            // Inflows: stored as positive
            validateAmount(request);
            calculatedAmount = request.getAmount();

        } else {
            // DEBIT, FEE: outflows stored as negative
            validateAmount(request);
            calculatedAmount = request.getAmount().negate();
        }

        CashLedger cashLedger = new CashLedger();
        cashLedger.setAccount(account);
        cashLedger.setTxnType(request.getTxnType());
        cashLedger.setAmount(calculatedAmount);
        cashLedger.setCurrency(request.getCurrency());
        cashLedger.setTxnDate(request.getTxnDate());
        cashLedger.setNarrative(request.getNarrative());
        CashLedger saved = cashLedgerRepository.save(cashLedger);
        return mapper.map(saved, CashLedgerResponse.class);
    }

    private void validateSecurityQuantityPrice(CashLedgerRequest request) {
        if (request.getSecurityId() == null) {
            throw new BadRequestException("securityId is required for " + request.getTxnType() + " transaction.");
        }
        if (request.getQuantity() == null) {
            throw new BadRequestException("quantity is required for " + request.getTxnType() + " transaction.");
        }
        if (request.getPrice() == null) {
            throw new BadRequestException("price is required for " + request.getTxnType() + " transaction.");
        }
    }

    private void validateAmount(CashLedgerRequest request) {
        if (request.getAmount() == null) {
            throw new BadRequestException("amount is required for " + request.getTxnType() + " transaction.");
        }
    }

    private void processSubscription(Account account, CashLedgerRequest request) {
        Optional<Holding> optionalHolding = holdingRepository.findByAccountAccountIdAndSecurityId(
                account.getAccountId(), request.getSecurityId());

        if (optionalHolding.isPresent()) {
            Holding holding = optionalHolding.get();
            BigDecimal oldQuantity = holding.getQuantity();
            BigDecimal oldAvgCost = holding.getAvgCost();
            BigDecimal newQuantity = oldQuantity.add(request.getQuantity());
            BigDecimal newAvgCost = (oldQuantity.multiply(oldAvgCost)
                    .add(request.getQuantity().multiply(request.getPrice())))
                    .divide(newQuantity, 4, RoundingMode.HALF_UP);
            holding.setQuantity(newQuantity);
            holding.setAvgCost(newAvgCost);
            holding.setLastValuationDate(request.getTxnDate());
            holdingRepository.save(holding);
        } else {
            Holding holding = new Holding();
            holding.setAccount(account);
            holding.setSecurityId(request.getSecurityId());
            holding.setQuantity(request.getQuantity());
            holding.setAvgCost(request.getPrice());
            holding.setValuationCurrency(account.getBaseCurrency());
            holding.setLastValuationDate(request.getTxnDate());
            holdingRepository.save(holding);
        }
    }

    private void processRedemption(Account account, CashLedgerRequest request) {
        Optional<Holding> optionalHolding = holdingRepository.findByAccountAccountIdAndSecurityId(
                account.getAccountId(), request.getSecurityId());

        if (optionalHolding.isEmpty()) {
            throw new BadRequestException("No holding found for securityId: " + request.getSecurityId()
                    + " under accountId: " + account.getAccountId() + ". Cannot redeem.");
        }
        Holding holding = optionalHolding.get();
        if (holding.getQuantity().compareTo(request.getQuantity()) < 0) {
            throw new BadRequestException("Insufficient holding quantity. Available: "
                    + holding.getQuantity() + ", Requested: " + request.getQuantity());
        }
        BigDecimal remainingQuantity = holding.getQuantity().subtract(request.getQuantity());
        holding.setQuantity(remainingQuantity);
        holding.setLastValuationDate(request.getTxnDate());
        holdingRepository.save(holding);
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
    public BigDecimal getBalanceByAccountId(Long accountId) {
        Optional<Account> optional = accountRepository.findById(accountId);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with id: " + accountId);
        }
        return cashLedgerRepository.sumAmountByAccountId(accountId);
    }

    @Override
    public CashLedgerResponse updateCashLedgerEntry(Long ledgerId, CashLedgerRequest request) {
        throw new BadRequestException("Update is not allowed.");
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
