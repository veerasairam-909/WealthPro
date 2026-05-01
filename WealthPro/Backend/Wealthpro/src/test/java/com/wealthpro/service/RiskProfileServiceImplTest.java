package com.wealthpro.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthpro.dto.request.RiskProfileRequestDTO;
import com.wealthpro.dto.response.RiskProfileResponseDTO;
import com.wealthpro.entities.Client;
import com.wealthpro.entities.RiskProfile;
import com.wealthpro.enums.ClientSegment;
import com.wealthpro.enums.ClientStatus;
import com.wealthpro.enums.RiskClass;
import com.wealthpro.exception.DuplicateResourceException;
import com.wealthpro.exception.InvalidOperationException;
import com.wealthpro.exception.ResourceNotFoundException;
import com.wealthpro.repositories.RiskProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RiskProfileServiceImplTest {

    @Mock
    private RiskProfileRepository riskProfileRepository;

    @Mock
    private ClientService clientService;   // interface

    // ObjectMapper is NOT mocked — we use real one
    // because it just converts Map to JSON string
    // no DB or external calls involved
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private RiskProfileServiceImpl riskProfileService;

    private Client client;
    private RiskProfile riskProfile;
    private RiskProfileRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        // Inject real ObjectMapper manually since
        // @InjectMocks won't pick it up without @Mock
        riskProfileService = new RiskProfileServiceImpl(
                riskProfileRepository,
                clientService,
                objectMapper
        );

        client = new Client();
        client.setClientId(1L);
        client.setName("Murali Krishna");
        client.setDob(LocalDate.of(1995, 6, 15));
        client.setContactInfo("murali@gmail.com");
        client.setSegment(ClientSegment.HNI);
        client.setStatus(ClientStatus.Active);

        // Answers: C=30, D=40, C=30, B=20, C=30 → total=150 ÷ 2 = 75 → Aggressive
        Map<String, String> answers = new HashMap<>();
        answers.put("q1", "C");
        answers.put("q2", "D");
        answers.put("q3", "C");
        answers.put("q4", "B");
        answers.put("q5", "C");

        requestDTO = new RiskProfileRequestDTO();
        requestDTO.setAnswers(answers);
        requestDTO.setAssessedDate(LocalDate.now());

        riskProfile = new RiskProfile();
        riskProfile.setRiskId(1L);
        riskProfile.setClient(client);
        riskProfile.setQuestionnaireJSON("{\"q1\":\"C\",\"q2\":\"D\",\"q3\":\"C\",\"q4\":\"B\",\"q5\":\"C\"}");
        riskProfile.setRiskScore(BigDecimal.valueOf(75.00));
        riskProfile.setRiskClass(RiskClass.Aggressive);
        riskProfile.setAssessedDate(LocalDate.now());
    }

    // ─────────────────────────────────────────
    // TEST 1: Create risk profile — success
    // ─────────────────────────────────────────
    @Test
    void testCreateRiskProfile_Success() {
        // Arrange
        when(clientService.findClientOrThrow(1L)).thenReturn(client);
        when(riskProfileRepository.existsByClientClientId(1L)).thenReturn(false);
        when(riskProfileRepository.save(any(RiskProfile.class))).thenReturn(riskProfile);

        // Act
        RiskProfileResponseDTO result =
                riskProfileService.createRiskProfile(1L, requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(RiskClass.Aggressive, result.getRiskClass());
        assertEquals(0, BigDecimal.valueOf(75.00).compareTo(result.getRiskScore()));

        verify(riskProfileRepository, times(1)).save(any(RiskProfile.class));
    }

    // ─────────────────────────────────────────
    // TEST 2: Create risk profile — duplicate → exception
    // ─────────────────────────────────────────
    @Test
    void testCreateRiskProfile_Duplicate_ThrowsException() {
        // Arrange — profile already exists for this client
        when(clientService.findClientOrThrow(1L)).thenReturn(client);
        when(riskProfileRepository.existsByClientClientId(1L)).thenReturn(true);

        // Act + Assert
        assertThrows(DuplicateResourceException.class, () -> {
            riskProfileService.createRiskProfile(1L, requestDTO);
        });

        // Verify save was never called
        verify(riskProfileRepository, never()).save(any());
    }

    // ─────────────────────────────────────────
    // TEST 3: Score calculation — All A → Conservative
    // A=10 x 5 = 50 ÷ 2 = 25 → Conservative
    // ─────────────────────────────────────────
    @Test
    void testCreateRiskProfile_AllA_Conservative() {
        // Arrange
        Map<String, String> allAAnswers = new HashMap<>();
        allAAnswers.put("q1", "A");
        allAAnswers.put("q2", "A");
        allAAnswers.put("q3", "A");
        allAAnswers.put("q4", "A");
        allAAnswers.put("q5", "A");

        requestDTO.setAnswers(allAAnswers);

        RiskProfile conservativeProfile = new RiskProfile();
        conservativeProfile.setRiskId(2L);
        conservativeProfile.setClient(client);
        conservativeProfile.setRiskScore(BigDecimal.valueOf(25.00));
        conservativeProfile.setRiskClass(RiskClass.Conservative);
        conservativeProfile.setAssessedDate(LocalDate.now());

        when(clientService.findClientOrThrow(1L)).thenReturn(client);
        when(riskProfileRepository.existsByClientClientId(1L)).thenReturn(false);
        when(riskProfileRepository.save(any(RiskProfile.class)))
                .thenReturn(conservativeProfile);

        // Act
        RiskProfileResponseDTO result =
                riskProfileService.createRiskProfile(1L, requestDTO);

        // Assert — score 25 → Conservative
        assertNotNull(result);
        assertEquals(RiskClass.Conservative, result.getRiskClass());
    }

    // ─────────────────────────────────────────
    // TEST 4: Score calculation — All B → Balanced
    // B=20 x 5 = 100 ÷ 2 = 50 → Balanced
    // ─────────────────────────────────────────
    @Test
    void testCreateRiskProfile_AllB_Balanced() {
        // Arrange
        Map<String, String> allBAnswers = new HashMap<>();
        allBAnswers.put("q1", "B");
        allBAnswers.put("q2", "B");
        allBAnswers.put("q3", "B");
        allBAnswers.put("q4", "B");
        allBAnswers.put("q5", "B");

        requestDTO.setAnswers(allBAnswers);

        RiskProfile balancedProfile = new RiskProfile();
        balancedProfile.setRiskId(3L);
        balancedProfile.setClient(client);
        balancedProfile.setRiskScore(BigDecimal.valueOf(50.00));
        balancedProfile.setRiskClass(RiskClass.Balanced);
        balancedProfile.setAssessedDate(LocalDate.now());

        when(clientService.findClientOrThrow(1L)).thenReturn(client);
        when(riskProfileRepository.existsByClientClientId(1L)).thenReturn(false);
        when(riskProfileRepository.save(any(RiskProfile.class)))
                .thenReturn(balancedProfile);

        // Act
        RiskProfileResponseDTO result =
                riskProfileService.createRiskProfile(1L, requestDTO);

        // Assert — score 50 → Balanced
        assertNotNull(result);
        assertEquals(RiskClass.Balanced, result.getRiskClass());
    }

    // ─────────────────────────────────────────
    // TEST 5: Invalid answer → exception
    // ─────────────────────────────────────────
    @Test
    void testCreateRiskProfile_InvalidAnswer_ThrowsException() {
        // Arrange — q1 has invalid answer "E"
        Map<String, String> invalidAnswers = new HashMap<>();
        invalidAnswers.put("q1", "E"); // invalid!
        invalidAnswers.put("q2", "A");
        invalidAnswers.put("q3", "B");
        invalidAnswers.put("q4", "C");
        invalidAnswers.put("q5", "D");

        requestDTO.setAnswers(invalidAnswers);

        when(clientService.findClientOrThrow(1L)).thenReturn(client);
        when(riskProfileRepository.existsByClientClientId(1L)).thenReturn(false);

        // Act + Assert
        assertThrows(InvalidOperationException.class, () -> {
            riskProfileService.createRiskProfile(1L, requestDTO);
        });
    }


    // TEST 6: Get risk profile by client ID — success

    @Test
    void testGetRiskProfileByClientId_Success() {
        // Arrange
        when(clientService.findClientOrThrow(1L)).thenReturn(client);
        when(riskProfileRepository.findByClientClientId(1L))
                .thenReturn(Optional.of(riskProfile));


        RiskProfileResponseDTO result =
                riskProfileService.getRiskProfileByClientId(1L);

        assertNotNull(result);
        assertEquals(RiskClass.Aggressive, result.getRiskClass());
    }


    // TEST 7: Get risk profile — not found → exception

    @Test
    void testGetRiskProfileByClientId_NotFound_ThrowsException() {
        // Arrange
        when(clientService.findClientOrThrow(1L)).thenReturn(client);
        when(riskProfileRepository.findByClientClientId(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            riskProfileService.getRiskProfileByClientId(1L);
        });
    }


    // TEST 8: Delete risk profile — success

    @Test
    void testDeleteRiskProfile_Success() {
        // Arrange
        when(clientService.findClientOrThrow(1L)).thenReturn(client);
        when(riskProfileRepository.findByClientClientId(1L))
                .thenReturn(Optional.of(riskProfile));


        riskProfileService.deleteRiskProfile(1L);

        verify(riskProfileRepository, times(1))
                .deleteById(riskProfile.getRiskId());
    }
}