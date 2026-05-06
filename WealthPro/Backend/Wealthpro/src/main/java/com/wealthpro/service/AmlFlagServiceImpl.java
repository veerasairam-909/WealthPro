package com.wealthpro.service;

import com.wealthpro.dto.request.AmlFlagRequestDTO;
import com.wealthpro.dto.request.AmlFlagReviewRequestDTO;
import com.wealthpro.dto.response.AmlFlagResponseDTO;
import com.wealthpro.entities.AmlFlag;
import com.wealthpro.enums.AmlFlagStatus;
import com.wealthpro.enums.AmlFlagType;
import com.wealthpro.exception.ResourceNotFoundException;
import com.wealthpro.feign.NotificationFeignClient;
import com.wealthpro.feign.dto.NotificationRequestDTO;
import com.wealthpro.repositories.AmlFlagRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AmlFlagServiceImpl implements AmlFlagService {

    private final AmlFlagRepository amlFlagRepository;
    private final ModelMapper modelMapper;
    private final NotificationFeignClient notificationFeignClient;

    public AmlFlagServiceImpl(AmlFlagRepository amlFlagRepository,
                              ModelMapper modelMapper,
                              NotificationFeignClient notificationFeignClient) {
        this.amlFlagRepository = amlFlagRepository;
        this.modelMapper = modelMapper;
        this.notificationFeignClient = notificationFeignClient;
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
        flag.setRaisedByUserId(requestDTO.getRaisedByUserId()); // null-safe: optional field

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
                    + ". Valid values: OPEN, REVIEWED, CLOSED");
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
                    + ". Valid values: REVIEWED, CLOSED");
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
    public AmlFlagResponseDTO requestClosure(Long amlFlagId, String rmUsername) {
        AmlFlag flag = findOrThrow(amlFlagId);

        if (flag.getStatus() == AmlFlagStatus.CLOSED) {
            throw new IllegalStateException("AML flag is already CLOSED.");
        }

        // RM is requesting closure — do NOT change the status.
        // Only the compliance analyst can actually close the flag.
        // Just notify the compliance analyst who raised the flag.
        if (flag.getRaisedByUserId() != null) {
            try {
                String message = String.format(
                        "RM '%s' has investigated AML Flag %d (Client %d) and confirmed the case is resolved. " +
                        "Please review and close the flag if you agree.",
                        rmUsername, amlFlagId, flag.getClientId()
                );
                notificationFeignClient.sendNotification(new NotificationRequestDTO(
                        flag.getRaisedByUserId(), message, "Compliance"
                ));
                log.info("[AML] Closure-request notification sent to compliance userId={} for flag {} by RM={}",
                        flag.getRaisedByUserId(), amlFlagId, rmUsername);
            } catch (Exception e) {
                log.warn("[AML] Could not send closure-request notification for flag {}: {}", amlFlagId, e.getMessage());
            }
        }

        // Return flag unchanged — status is not modified by RM
        return toResponse(flag);
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
        dto.setRaisedByUserId(flag.getRaisedByUserId());
        return dto;
    }
}
