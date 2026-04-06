package com.wealth.pbor.service.impl;

import com.wealth.pbor.dto.request.CorporateActionRequest;
import com.wealth.pbor.dto.response.CorporateActionResponse;
import com.wealth.pbor.entity.Account;
import com.wealth.pbor.entity.CashLedger;
import com.wealth.pbor.entity.CorporateAction;
import com.wealth.pbor.entity.Holding;
import com.wealth.pbor.enums.CAType;
import com.wealth.pbor.enums.TxnType;
import com.wealth.pbor.exception.BadRequestException;
import com.wealth.pbor.exception.ResourceNotFoundException;
import com.wealth.pbor.repository.AccountRepository;
import com.wealth.pbor.repository.CashLedgerRepository;
import com.wealth.pbor.repository.CorporateActionRepository;
import com.wealth.pbor.repository.HoldingRepository;
import com.wealth.pbor.service.CorporateActionService;
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
public class CorporateActionServiceImpl implements CorporateActionService {

    private final CorporateActionRepository corporateActionRepository;
    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;
    private final CashLedgerRepository cashLedgerRepository;
    private final ModelMapper mapper;

    @Override
    public CorporateActionResponse createCorporateAction(CorporateActionRequest request) {
        if (request.getExDate().isAfter(request.getRecordDate())) {
            throw new BadRequestException("Ex-date cannot be after record date.");
        }
        if (!request.getPayDate().isAfter(request.getRecordDate())) {
            throw new BadRequestException("Pay date must be after record date.");
        }

        Optional<Account> optionalAccount = accountRepository.findById(request.getAccountId());
        if (optionalAccount.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with id: " + request.getAccountId());
        }
        Account account = optionalAccount.get();

        CorporateAction corporateAction = new CorporateAction();
        corporateAction.setSecurityId(request.getSecurityId());
        corporateAction.setCaType(request.getCaType());
        corporateAction.setRecordDate(request.getRecordDate());
        corporateAction.setExDate(request.getExDate());
        corporateAction.setPayDate(request.getPayDate());
        corporateAction.setTermsJson(request.getTermsJson());
        CorporateAction saved = corporateActionRepository.save(corporateAction);

        if (request.getCaType() == CAType.DIVIDEND) {
            processDividend(account, request);
        } else if (request.getCaType() == CAType.BONUS) {
            processBonus(account, request);
        } else if (request.getCaType() == CAType.SPLIT) {
            processSplit(account, request);
        } else if (request.getCaType() == CAType.REDEMPTION) {
            processCorporateRedemption(account, request);
        } else if (request.getCaType() == CAType.COUPON) {
            processCoupon(account, request);
        }

        return mapper.map(saved, CorporateActionResponse.class);
    }

    private void processDividend(Account account, CorporateActionRequest request) {
        if (request.getAmount() == null) {
            throw new BadRequestException("amount is required for DIVIDEND corporate action.");
        }
        if (request.getCurrency() == null) {
            throw new BadRequestException("currency is required for DIVIDEND corporate action.");
        }
        CashLedger cashLedger = new CashLedger();
        cashLedger.setAccount(account);
        cashLedger.setTxnType(TxnType.DIVIDEND);
        cashLedger.setAmount(request.getAmount());
        cashLedger.setCurrency(request.getCurrency());
        cashLedger.setTxnDate(request.getPayDate());
        cashLedger.setNarrative("Dividend received for securityId: " + request.getSecurityId());
        cashLedgerRepository.save(cashLedger);
    }

    private void processBonus(Account account, CorporateActionRequest request) {
        if (request.getBonusRatio() == null) {
            throw new BadRequestException("bonusRatio is required for BONUS corporate action. Example: 1 means 1:1 bonus.");
        }
        Optional<Holding> optionalHolding = holdingRepository.findByAccountAccountIdAndSecurityId(
                account.getAccountId(), request.getSecurityId());
        if (optionalHolding.isEmpty()) {
            throw new BadRequestException("No holding found for securityId: " + request.getSecurityId()
                    + " under accountId: " + account.getAccountId());
        }
        Holding holding = optionalHolding.get();
        BigDecimal oldQuantity = holding.getQuantity();
        BigDecimal bonusQuantity = oldQuantity.multiply(request.getBonusRatio());
        BigDecimal newQuantity = oldQuantity.add(bonusQuantity);
        BigDecimal newAvgCost = oldQuantity.multiply(holding.getAvgCost())
                .divide(newQuantity, 4, RoundingMode.HALF_UP);
        holding.setQuantity(newQuantity);
        holding.setAvgCost(newAvgCost);
        holding.setLastValuationDate(request.getPayDate());
        holdingRepository.save(holding);
    }

    private void processSplit(Account account, CorporateActionRequest request) {
        if (request.getSplitRatio() == null) {
            throw new BadRequestException("splitRatio is required for SPLIT corporate action. Example: 2 means quantity doubles.");
        }
        Optional<Holding> optionalHolding = holdingRepository.findByAccountAccountIdAndSecurityId(
                account.getAccountId(), request.getSecurityId());
        if (optionalHolding.isEmpty()) {
            throw new BadRequestException("No holding found for securityId: " + request.getSecurityId()
                    + " under accountId: " + account.getAccountId());
        }
        Holding holding = optionalHolding.get();
        BigDecimal newQuantity = holding.getQuantity().multiply(request.getSplitRatio());
        BigDecimal newAvgCost = holding.getAvgCost().divide(request.getSplitRatio(), 4, RoundingMode.HALF_UP);
        holding.setQuantity(newQuantity);
        holding.setAvgCost(newAvgCost);
        holding.setLastValuationDate(request.getPayDate());
        holdingRepository.save(holding);
    }

    private void processCorporateRedemption(Account account, CorporateActionRequest request) {
        if (request.getAmount() == null) {
            throw new BadRequestException("amount is required for REDEMPTION corporate action.");
        }
        if (request.getCurrency() == null) {
            throw new BadRequestException("currency is required for REDEMPTION corporate action.");
        }
        CashLedger cashLedger = new CashLedger();
        cashLedger.setAccount(account);
        cashLedger.setTxnType(TxnType.REDEMPTION);
        cashLedger.setAmount(request.getAmount());
        cashLedger.setCurrency(request.getCurrency());
        cashLedger.setTxnDate(request.getPayDate());
        cashLedger.setNarrative("Corporate redemption for securityId: " + request.getSecurityId());
        cashLedgerRepository.save(cashLedger);

        Optional<Holding> optionalHolding = holdingRepository.findByAccountAccountIdAndSecurityId(
                account.getAccountId(), request.getSecurityId());
        if (optionalHolding.isPresent()) {
            Holding holding = optionalHolding.get();
            holding.setQuantity(BigDecimal.ZERO);
            holding.setLastValuationDate(request.getPayDate());
            holdingRepository.save(holding);
        }
    }

    private void processCoupon(Account account, CorporateActionRequest request) {
        if (request.getAmount() == null) {
            throw new BadRequestException("amount is required for COUPON corporate action.");
        }
        if (request.getCurrency() == null) {
            throw new BadRequestException("currency is required for COUPON corporate action.");
        }
        CashLedger cashLedger = new CashLedger();
        cashLedger.setAccount(account);
        cashLedger.setTxnType(TxnType.DIVIDEND);
        cashLedger.setAmount(request.getAmount());
        cashLedger.setCurrency(request.getCurrency());
        cashLedger.setTxnDate(request.getPayDate());
        cashLedger.setNarrative("Coupon interest received for securityId: " + request.getSecurityId());
        cashLedgerRepository.save(cashLedger);
    }

    @Override
    public CorporateActionResponse getCorporateActionById(Long caId) {
        Optional<CorporateAction> optional = corporateActionRepository.findById(caId);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Corporate action not found with id: " + caId);
        }
        return mapper.map(optional.get(), CorporateActionResponse.class);
    }

    @Override
    public List<CorporateActionResponse> getAllCorporateActions() {
        List<CorporateAction> actions = corporateActionRepository.findAll();
        List<CorporateActionResponse> responseList = new ArrayList<>();
        for (CorporateAction action : actions) {
            responseList.add(mapper.map(action, CorporateActionResponse.class));
        }
        return responseList;
    }

    @Override
    public List<CorporateActionResponse> getCorporateActionsBySecurityId(Long securityId) {
        List<CorporateAction> actions = corporateActionRepository.findBySecurityId(securityId);
        List<CorporateActionResponse> responseList = new ArrayList<>();
        for (CorporateAction action : actions) {
            responseList.add(mapper.map(action, CorporateActionResponse.class));
        }
        return responseList;
    }

    @Override
    public List<CorporateActionResponse> getCorporateActionsByCaType(CAType caType) {
        List<CorporateAction> actions = corporateActionRepository.findByCaType(caType);
        List<CorporateActionResponse> responseList = new ArrayList<>();
        for (CorporateAction action : actions) {
            responseList.add(mapper.map(action, CorporateActionResponse.class));
        }
        return responseList;
    }

    @Override
    public List<CorporateActionResponse> getCorporateActionsByRecordDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BadRequestException("From date cannot be after to date.");
        }
        List<CorporateAction> actions = corporateActionRepository.findByRecordDateBetween(from, to);
        List<CorporateActionResponse> responseList = new ArrayList<>();
        for (CorporateAction action : actions) {
            responseList.add(mapper.map(action, CorporateActionResponse.class));
        }
        return responseList;
    }

    @Override
    public CorporateActionResponse updateCorporateAction(Long caId, CorporateActionRequest request) {
        Optional<CorporateAction> optional = corporateActionRepository.findById(caId);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Corporate action not found with id: " + caId);
        }
        if (request.getExDate().isAfter(request.getRecordDate())) {
            throw new BadRequestException("Ex-date cannot be after record date.");
        }
        if (!request.getPayDate().isAfter(request.getRecordDate())) {
            throw new BadRequestException("Pay date must be after record date.");
        }
        CorporateAction corporateAction = optional.get();
        corporateAction.setSecurityId(request.getSecurityId());
        corporateAction.setCaType(request.getCaType());
        corporateAction.setRecordDate(request.getRecordDate());
        corporateAction.setExDate(request.getExDate());
        corporateAction.setPayDate(request.getPayDate());
        corporateAction.setTermsJson(request.getTermsJson());
        CorporateAction updated = corporateActionRepository.save(corporateAction);
        return mapper.map(updated, CorporateActionResponse.class);
    }

    @Override
    public void deleteCorporateAction(Long caId) {
        Optional<CorporateAction> optional = corporateActionRepository.findById(caId);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Corporate action not found with id: " + caId);
        }
        corporateActionRepository.delete(optional.get());
    }
}
