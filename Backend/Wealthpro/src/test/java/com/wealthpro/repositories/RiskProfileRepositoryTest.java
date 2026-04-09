package com.wealthpro.repositories;

import com.wealthpro.entities.Client;
import com.wealthpro.entities.RiskProfile;
import com.wealthpro.enums.ClientSegment;
import com.wealthpro.enums.ClientStatus;
import com.wealthpro.enums.RiskClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class RiskProfileRepositoryTest {

    @Autowired
    private RiskProfileRepository riskProfileRepository;

    @Autowired
    private ClientRepository clientRepository;

    private Client savedClient;

    @BeforeEach
    void setUp() {
        Client client = new Client();
        client.setName("Murali Krishna");
        client.setDob(LocalDate.of(1995, 6, 15));
        client.setContactInfo("murali@gmail.com");
        client.setSegment(ClientSegment.HNI);
        client.setStatus(ClientStatus.Active);
        savedClient = clientRepository.save(client);
    }

    // ─────────────────────────────────────────
    // TEST 1: Save RiskProfile
    // ─────────────────────────────────────────
    @Test
    void testSaveRiskProfile_Success() {
        RiskProfile riskProfile = new RiskProfile();
        riskProfile.setClient(savedClient);
        riskProfile.setQuestionnaireJSON("{\"q1\":\"C\",\"q2\":\"D\"}");
        riskProfile.setRiskScore(BigDecimal.valueOf(75.00));
        riskProfile.setRiskClass(RiskClass.Aggressive);
        riskProfile.setAssessedDate(LocalDate.now());

        RiskProfile saved = riskProfileRepository.save(riskProfile);

        assertNotNull(saved);
        assertNotNull(saved.getRiskId());
        assertEquals(RiskClass.Aggressive, saved.getRiskClass());
        assertEquals(0, BigDecimal.valueOf(75.00)
                .compareTo(saved.getRiskScore()));
    }

    // ─────────────────────────────────────────
    // TEST 2: Find RiskProfile by client ID
    // ─────────────────────────────────────────
    @Test
    void testFindByClientClientId_ReturnsRiskProfile() {
        RiskProfile riskProfile = new RiskProfile();
        riskProfile.setClient(savedClient);
        riskProfile.setQuestionnaireJSON("{\"q1\":\"A\"}");
        riskProfile.setRiskScore(BigDecimal.valueOf(25.00));
        riskProfile.setRiskClass(RiskClass.Conservative);
        riskProfile.setAssessedDate(LocalDate.now());
        riskProfileRepository.save(riskProfile);

        Optional<RiskProfile> found = riskProfileRepository
                .findByClientClientId(savedClient.getClientId());

        assertTrue(found.isPresent());
        assertEquals(RiskClass.Conservative, found.get().getRiskClass());
    }

    // ─────────────────────────────────────────
    // TEST 3: existsByClientClientId — true
    // ─────────────────────────────────────────
    @Test
    void testExistsByClientClientId_WhenExists_ReturnsTrue() {
        RiskProfile riskProfile = new RiskProfile();
        riskProfile.setClient(savedClient);
        riskProfile.setQuestionnaireJSON("{\"q1\":\"B\"}");
        riskProfile.setRiskScore(BigDecimal.valueOf(50.00));
        riskProfile.setRiskClass(RiskClass.Balanced);
        riskProfile.setAssessedDate(LocalDate.now());
        riskProfileRepository.save(riskProfile);

        boolean exists = riskProfileRepository
                .existsByClientClientId(savedClient.getClientId());

        assertTrue(exists);
    }

    // ─────────────────────────────────────────
    // TEST 4: existsByClientClientId — false
    // ─────────────────────────────────────────
    @Test
    void testExistsByClientClientId_WhenNotExists_ReturnsFalse() {
        boolean exists = riskProfileRepository
                .existsByClientClientId(savedClient.getClientId());

        assertFalse(exists);
    }

    // ─────────────────────────────────────────
    // TEST 5: Find by client ID — not found
    // ─────────────────────────────────────────
    @Test
    void testFindByClientClientId_WhenNotExists_ReturnsEmpty() {
        Optional<RiskProfile> found = riskProfileRepository
                .findByClientClientId(999L);

        assertFalse(found.isPresent());
    }

    // ─────────────────────────────────────────
    // TEST 6: Delete RiskProfile
    // ─────────────────────────────────────────
    @Test
    void testDeleteRiskProfile_Success() {
        RiskProfile riskProfile = new RiskProfile();
        riskProfile.setClient(savedClient);
        riskProfile.setQuestionnaireJSON("{\"q1\":\"C\"}");
        riskProfile.setRiskScore(BigDecimal.valueOf(75.00));
        riskProfile.setRiskClass(RiskClass.Aggressive);
        riskProfile.setAssessedDate(LocalDate.now());
        RiskProfile saved = riskProfileRepository.save(riskProfile);

        riskProfileRepository.deleteById(saved.getRiskId());

        Optional<RiskProfile> found = riskProfileRepository
                .findById(saved.getRiskId());
        assertFalse(found.isPresent());
    }
}