package com.wealth.pbor.service.impl;

import com.wealth.pbor.dto.request.HoldingRequest;
import com.wealth.pbor.dto.response.HoldingResponse;
import com.wealth.pbor.entity.Account;
import com.wealth.pbor.entity.Holding;
import com.wealth.pbor.exception.BadRequestException;
import com.wealth.pbor.exception.ResourceNotFoundException;
import com.wealth.pbor.repository.AccountRepository;
import com.wealth.pbor.repository.HoldingRepository;
import com.wealth.pbor.service.HoldingService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HoldingServiceImpl implements HoldingService {

    private final HoldingRepository holdingRepository;
    private final AccountRepository accountRepository;
    private final ModelMapper mapper;

    @Override
    public HoldingResponse createHolding(HoldingRequest request) {
        Optional<Account> optionalAccount = accountRepository.findById(request.getAccountId());
        if (optionalAccount.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with id: " + request.getAccountId());
        }
        boolean exists = holdingRepository.existsByAccountAccountIdAndSecurityId(
                request.getAccountId(), request.getSecurityId());
        if (exists) {
            throw new BadRequestException("A holding for security ID " + request.getSecurityId()
                    + " already exists under account ID " + request.getAccountId() + ". Please update the existing holding instead.");
        }
        Holding holding = new Holding();
        holding.setAccount(optionalAccount.get());
        holding.setSecurityId(request.getSecurityId());
        holding.setQuantity(request.getQuantity());
        holding.setAvgCost(request.getAvgCost());
        holding.setValuationCurrency(request.getValuationCurrency());
        holding.setLastValuationDate(request.getLastValuationDate());
        Holding saved = holdingRepository.save(holding);
        return mapper.map(saved, HoldingResponse.class);
    }

    @Override
    public HoldingResponse getHoldingById(Long holdingId) {
        Optional<Holding> optional = holdingRepository.findById(holdingId);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Holding not found with id: " + holdingId);
        }
        return mapper.map(optional.get(), HoldingResponse.class);
    }

    @Override
    public List<HoldingResponse> getAllHoldings() {
        List<Holding> holdings = holdingRepository.findAll();
        List<HoldingResponse> responseList = new ArrayList<>();
        for (Holding holding : holdings) {
            responseList.add(mapper.map(holding, HoldingResponse.class));
        }
        return responseList;
    }

    @Override
    public List<HoldingResponse> getHoldingsByAccountId(Long accountId) {
        Optional<Account> optional = accountRepository.findById(accountId);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with id: " + accountId);
        }
        List<Holding> holdings = holdingRepository.findByAccountAccountId(accountId);
        List<HoldingResponse> responseList = new ArrayList<>();
        for (Holding holding : holdings) {
            responseList.add(mapper.map(holding, HoldingResponse.class));
        }
        return responseList;
    }

    @Override
    public List<HoldingResponse> getHoldingsBySecurityId(Long securityId) {
        List<Holding> holdings = holdingRepository.findBySecurityId(securityId);
        List<HoldingResponse> responseList = new ArrayList<>();
        for (Holding holding : holdings) {
            responseList.add(mapper.map(holding, HoldingResponse.class));
        }
        return responseList;
    }

    @Override
    public HoldingResponse updateHolding(Long holdingId, HoldingRequest request) {
        Optional<Holding> optionalHolding = holdingRepository.findById(holdingId);
        if (optionalHolding.isEmpty()) {
            throw new ResourceNotFoundException("Holding not found with id: " + holdingId);
        }
        Optional<Account> optionalAccount = accountRepository.findById(request.getAccountId());
        if (optionalAccount.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with id: " + request.getAccountId());
        }
        Holding holding = optionalHolding.get();
        boolean changed = !holding.getSecurityId().equals(request.getSecurityId())
                || !holding.getAccount().getAccountId().equals(request.getAccountId());
        if (changed) {
            boolean exists = holdingRepository.existsByAccountAccountIdAndSecurityId(
                    request.getAccountId(), request.getSecurityId());
            if (exists) {
                throw new BadRequestException("A holding for security ID " + request.getSecurityId()
                        + " already exists under account ID " + request.getAccountId() + ". Please update the existing holding instead.");
            }
        }
        holding.setAccount(optionalAccount.get());
        holding.setSecurityId(request.getSecurityId());
        holding.setQuantity(request.getQuantity());
        holding.setAvgCost(request.getAvgCost());
        holding.setValuationCurrency(request.getValuationCurrency());
        holding.setLastValuationDate(request.getLastValuationDate());
        Holding updated = holdingRepository.save(holding);
        return mapper.map(updated, HoldingResponse.class);
    }

    @Override
    public void deleteHolding(Long holdingId) {
        Optional<Holding> optional = holdingRepository.findById(holdingId);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Holding not found with id: " + holdingId);
        }
        holdingRepository.delete(optional.get());
    }
}
