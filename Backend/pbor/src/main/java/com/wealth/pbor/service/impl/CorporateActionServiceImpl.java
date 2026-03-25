package com.wealth.pbor.service.impl;

import com.wealth.pbor.dto.request.CorporateActionRequest;
import com.wealth.pbor.dto.response.CorporateActionResponse;
import com.wealth.pbor.entity.CorporateAction;
import com.wealth.pbor.enums.CAType;
import com.wealth.pbor.exception.BadRequestException;
import com.wealth.pbor.exception.ResourceNotFoundException;
import com.wealth.pbor.repository.CorporateActionRepository;
import com.wealth.pbor.service.CorporateActionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CorporateActionServiceImpl implements CorporateActionService {

    private final CorporateActionRepository corporateActionRepository;
    private final ModelMapper mapper;

    @Override
    public CorporateActionResponse createCorporateAction(CorporateActionRequest request) {
        if (request.getExDate().isAfter(request.getRecordDate())) {
            throw new BadRequestException("Ex-date cannot be after record date.");
        }
        if (!request.getPayDate().isAfter(request.getRecordDate())) {
            throw new BadRequestException("Pay date must be after record date.");
        }
        CorporateAction corporateAction = new CorporateAction();
        corporateAction.setSecurityId(request.getSecurityId());
        corporateAction.setCaType(request.getCaType());
        corporateAction.setRecordDate(request.getRecordDate());
        corporateAction.setExDate(request.getExDate());
        corporateAction.setPayDate(request.getPayDate());
        corporateAction.setTermsJson(request.getTermsJson());
        CorporateAction saved = corporateActionRepository.save(corporateAction);
        return mapper.map(saved, CorporateActionResponse.class);
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