package com.wealthpro.service;

import com.wealthpro.dto.request.AmlFlagRequestDTO;
import com.wealthpro.dto.request.AmlFlagReviewRequestDTO;
import com.wealthpro.dto.response.AmlFlagResponseDTO;

import java.util.List;

public interface AmlFlagService {

    AmlFlagResponseDTO createFlag(AmlFlagRequestDTO requestDTO);

    List<AmlFlagResponseDTO> getAllFlags();

    List<AmlFlagResponseDTO> getFlagsByClient(Long clientId);

    List<AmlFlagResponseDTO> getFlagsByStatus(String status);

    AmlFlagResponseDTO getFlagById(Long amlFlagId);

    AmlFlagResponseDTO reviewFlag(Long amlFlagId, AmlFlagReviewRequestDTO reviewDTO, String reviewedBy);

    void deleteFlag(Long amlFlagId);
}
