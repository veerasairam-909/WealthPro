package com.wealthpro.service;

import com.wealthpro.dto.request.AmlFlagRequestDTO;
import com.wealthpro.dto.request.AmlFlagReviewRequestDTO;
import com.wealthpro.dto.response.AmlFlagResponseDTO;
import com.wealthpro.entities.AmlFlag;
import com.wealthpro.enums.AmlFlagStatus;
import com.wealthpro.enums.AmlFlagType;
import com.wealthpro.exception.ResourceNotFoundException;
import com.wealthpro.repositories.AmlFlagRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AmlFlagServiceImpl implements AmlFlagService {

    private final AmlFlagRepository amlFlagRepository;
    private final ModelMapper modelMapper;

    public AmlFlagServiceImpl(AmlFlagRepository amlFlagRepository, ModelMapper modelMapper) {
        this.amlFlagRepository = amlFlagRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public AmlFlagResponseDTO createFlag(AmlFlagRequestDTO requestDTO) {
        AmlFlagType flagType;
        try {
            flagType = AmlFlagType.valueOf(requestDTO.getFlagType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid flag type: " + requestDTO.getFlagType()
                    + ". Valid values: SUSPICIOUS_TRANSACTION, HIGH_VALUE_TRANSFER, UNUSUAL_PATTERN, WATCHLIST_MATCH, MANUAL");
        }

        AmlFlag flag = new AmlFlag();
        flag.setClientId(requestDTO.getClientId());
        flag.setFlagType(flagType);
        flag.setDescription(requestDTO.getDescription());
        flag.setNotes(requestDTO.getNotes());
        flag.setStatus(AmlFlagStatus.OPEN);
        flag.setFlaggedDate(LocalDate.now());

        AmlFlag saved = amlFlagRepository.save(flag);
        return toResponse(saved);
    }

    @Override
    public List<AmlFlagResponseDTO> getAllFlags() {
        return amlFlagRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AmlFlagResponseDTO> getFlagsByClient(Long clientId) {
        return amlFlagRepository.findByClientId(clientId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AmlFlagResponseDTO> getFlagsByStatus(String status) {
        AmlFlagStatus flagStatus;
        try {
            flagStatus = AmlFlagStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status
                    + ". Valid values: OPEN, REVIEWED, CLEARED, ESCALATED");
        }
        return amlFlagRepository.findByStatus(flagStatus).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AmlFlagResponseDTO getFlagById(Long amlFlagId) {
        AmlFlag flag = findOrThrow(amlFlagId);
        return toResponse(flag);
    }

    @Override
    public AmlFlagResponseDTO reviewFlag(Long amlFlagId, AmlFlagReviewRequestDTO reviewDTO, String reviewedBy) {
        AmlFlag flag = findOrThrow(amlFlagId);

        AmlFlagStatus newStatus;
        try {
            newStatus = AmlFlagStatus.valueOf(reviewDTO.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + reviewDTO.getStatus()
                    + ". Valid values: REVIEWED, CLEARED, ESCALATED");
        }
        flag.setStatus(newStatus);
        flag.setReviewedBy(reviewedBy);
        flag.setReviewedDate(LocalDate.now());
        if (reviewDTO.getNotes() != null) {
            flag.setNotes(reviewDTO.getNotes());
        }

        AmlFlag updated = amlFlagRepository.save(flag);
        return toResponse(updated);
    }

    @Override
    public void deleteFlag(Long amlFlagId) {
        findOrThrow(amlFlagId);
        amlFlagRepository.deleteById(amlFlagId);
    }

    private AmlFlag findOrThrow(Long amlFlagId) {
        return amlFlagRepository.findById(amlFlagId)
                .orElseThrow(() -> new ResourceNotFoundException("AML flag not found with ID: " + amlFlagId));
    }

    private AmlFlagResponseDTO toResponse(AmlFlag flag) {
        AmlFlagResponseDTO dto = new AmlFlagResponseDTO();
        dto.setAmlFlagId(flag.getAmlFlagId());
        dto.setClientId(flag.getClientId());
        dto.setFlagType(flag.getFlagType() != null ? flag.getFlagType().name() : null);
        dto.setDescription(flag.getDescription());
        dto.setStatus(flag.getStatus() != null ? flag.getStatus().name() : null);
        dto.setFlaggedDate(flag.getFlaggedDate());
        dto.setReviewedBy(flag.getReviewedBy());
        dto.setReviewedDate(flag.getReviewedDate());
        dto.setNotes(flag.getNotes());
        return dto;
    }
}
