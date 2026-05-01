package com.wealthpro.service;

import com.wealthpro.dto.request.RiskProfileRequestDTO;
import com.wealthpro.dto.response.RiskProfileResponseDTO;
import com.wealthpro.entities.Client;
import com.wealthpro.entities.RiskProfile;
import com.wealthpro.enums.RiskClass;
import com.wealthpro.exception.DuplicateResourceException;
import com.wealthpro.exception.InvalidOperationException;
import com.wealthpro.exception.ResourceNotFoundException;
import com.wealthpro.repositories.RiskProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;

@Service
public class RiskProfileServiceImpl implements RiskProfileService{

    private final RiskProfileRepository riskProfileRepository;
    private final ClientService clientService;
    private final ObjectMapper objectMapper;

    public RiskProfileServiceImpl(RiskProfileRepository riskProfileRepository,
                                  ClientService clientService,
                                  ObjectMapper objectMapper) {
        this.riskProfileRepository = riskProfileRepository;
        this.clientService = clientService;
        this.objectMapper = objectMapper;
    }


    //  Example Questions and their valid answers:

    // Q1: Investment goal
    //     A=Capital preservation, B=Steady income,
    //     C=Balanced growth,      D=Maximum growth

    // Q2: Investment horizon
    //     A=< 1 year, B=1-3 years, C=3-7 years, D=> 7 years

    // Q3: Reaction to 20% portfolio drop
    //     A=Sell everything, B=Sell some,
    //     C=Hold & wait,     D=Buy more

    // Q4: % of savings being invested
    //     A=< 10%, B=10-30%, C=30-60%, D=> 60%

    // Q5: Investment experience
    //     A=None, B=Basic (FDs), C=Intermediate (MFs), D=Advanced (Stocks)

    // Scoring per answer:
    //     A = 10 points (most conservative)
    //     B = 20 points
    //     C = 30 points
    //     D = 40 points (most aggressive)

    // Max score = 5 x 40 = 200
    // Normalized to 100 by dividing by 2

    // RiskClass:
    //     0  - 40  → Conservative
    //     41 - 70  → Balanced
    //     71 - 100 → Aggressive


    // creating risk profile for a client

    public RiskProfileResponseDTO createRiskProfile(Long clientId,
                                                    RiskProfileRequestDTO requestDTO) {
        // Step 1: Check client exists
        Client client = clientService.findClientOrThrow(clientId);

        // Step 2: Business rule — one client = one risk profile
        if (riskProfileRepository.existsByClientClientId(clientId)) {
            throw new DuplicateResourceException(
                    "Risk profile already exists for client ID: " + clientId
                            + ". Use update endpoint to modify it.");
        }

        // Step 3: Validate answers
        validateAnswers(requestDTO.getAnswers());

        // Step 4: Calculate score from answers
        BigDecimal riskScore = calculateScore(requestDTO.getAnswers());

        // Step 5: Derive RiskClass from score
        RiskClass riskClass = calculateRiskClass(riskScore);

        // Step 6: Convert answers map → JSON string to store in DB
        String questionnaireJSON = convertAnswersToJSON(requestDTO.getAnswers());

        // Step 7: Build and save entity
        RiskProfile riskProfile = new RiskProfile();
        riskProfile.setClient(client);
        riskProfile.setQuestionnaireJSON(questionnaireJSON);
        riskProfile.setRiskScore(riskScore);
        riskProfile.setRiskClass(riskClass);
        riskProfile.setAssessedDate(requestDTO.getAssessedDate());

        RiskProfile saved = riskProfileRepository.save(riskProfile);
        return mapToResponse(saved);
    }

    // GET risk profile by client ID

    public RiskProfileResponseDTO getRiskProfileByClientId(Long clientId) {
        clientService.findClientOrThrow(clientId);

        Optional<RiskProfile> opt = riskProfileRepository.findByClientClientId(clientId);
        if (opt.isPresent()) {
            return mapToResponse(opt.get());
        } else {
            throw new ResourceNotFoundException("Risk profile not found for client ID: " + clientId);
        }
    }

    // GET risk profile by risk ID
    public RiskProfileResponseDTO getRiskProfileById(Long riskId) {
        return mapToResponse(findRiskProfileOrThrow(riskId));
    }

    // UPDATE risk profile — recalculate from new answers
    public RiskProfileResponseDTO updateRiskProfile(Long clientId,
                                                    RiskProfileRequestDTO requestDTO) {
        clientService.findClientOrThrow(clientId);

        Optional<RiskProfile> opt = riskProfileRepository.findByClientClientId(clientId);
        RiskProfile existing;
        if (opt.isPresent()) {
            existing = opt.get();
        } else {
            throw new ResourceNotFoundException(
                    "Risk profile not found for client ID: " + clientId + ". Create one first.");
        }

        // Validate new answers
        validateAnswers(requestDTO.getAnswers());

        // Recalculate everything from new answers
        BigDecimal newScore = calculateScore(requestDTO.getAnswers());
        RiskClass newClass = calculateRiskClass(newScore);
        String newJSON    = convertAnswersToJSON(requestDTO.getAnswers());

        existing.setQuestionnaireJSON(newJSON);
        existing.setRiskScore(newScore);
        existing.setRiskClass(newClass);
        existing.setAssessedDate(requestDTO.getAssessedDate());

        RiskProfile updated = riskProfileRepository.save(existing);
        return mapToResponse(updated);
    }

    // DELETE risk profile

    public void deleteRiskProfile(Long clientId) {
        clientService.findClientOrThrow(clientId);

        Optional<RiskProfile> opt = riskProfileRepository.findByClientClientId(clientId);
        RiskProfile riskProfile;
        if (opt.isPresent()) {
            riskProfile = opt.get();
        } else {
            throw new ResourceNotFoundException("Risk profile not found for client ID: " + clientId);
        }

        riskProfileRepository.deleteById(riskProfile.getRiskId());
    }


    // Validate answers

    private void validateAnswers(Map<String, String> answers) {

        String[] requiredKeys = {"q1", "q2", "q3", "q4", "q5"};

        for (String key : requiredKeys) {
            if (!answers.containsKey(key)) {
                throw new InvalidOperationException(
                        "Missing answer for question: " + key);
            }
            String answer = answers.get(key).toUpperCase();
            if (!answer.equals("A") && !answer.equals("B")
                    && !answer.equals("C") && !answer.equals("D")) {
                throw new InvalidOperationException(
                        "Invalid answer for " + key + ": '" + answers.get(key)
                                + "'. Only A, B, C, D are allowed.");
            }
        }
    }


    //  Calculate score from answers
    // A=10, B=20, C=30, D=40
    // Total out of 200, normalized to 100

    private BigDecimal calculateScore(Map<String, String> answers) {
        int total = 0;

        for (String raw : answers.values()) {
            String answer = raw == null ? "" : raw.toUpperCase();
            switch (answer) {
                case "A":
                    total += 10;
                    break;
                case "B":
                    total += 20;
                    break;
                case "C":
                    total += 30;
                    break;
                case "D":
                    total += 40;
                    break;
                default:
                    break;
            }
        }

        // divide by 2 to bring max from 200 → 100
        return BigDecimal.valueOf(total)
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }


    // We classify RiskClass from  score
    // 0  - 40  → Conservative
    // 41 - 70  → Balanced
    // 71 - 100 → Aggressive

    private RiskClass calculateRiskClass(BigDecimal score) {
        int s = score.intValue();
        if (s <= 40) return RiskClass.Conservative;
        if (s <= 70) return RiskClass.Balanced;
        return RiskClass.Aggressive;
    }


    //  Convert answers Map to JSON string for DB storage

    private String convertAnswersToJSON(Map<String, String> answers) {
        try {
            return objectMapper.writeValueAsString(answers);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert answers to JSON: " + e.getMessage());
        }
    }


    //  Find RiskProfile or throw 404

    private RiskProfile findRiskProfileOrThrow(Long riskId) {
        Optional<RiskProfile> opt = riskProfileRepository.findById(riskId);
        if (opt.isPresent()) {
            return opt.get();
        } else {
            throw new ResourceNotFoundException("Risk profile not found with ID: " + riskId);
        }
    }

    // Map Entity to Response DTO
    private RiskProfileResponseDTO mapToResponse(RiskProfile riskProfile) {
        RiskProfileResponseDTO response = new RiskProfileResponseDTO();
        response.setRiskId(riskProfile.getRiskId());
        response.setClientId(riskProfile.getClient().getClientId());
        response.setQuestionnaireJSON(riskProfile.getQuestionnaireJSON());
        response.setRiskScore(riskProfile.getRiskScore());
        response.setRiskClass(riskProfile.getRiskClass());
        response.setAssessedDate(riskProfile.getAssessedDate());
        return response;
    }
}
