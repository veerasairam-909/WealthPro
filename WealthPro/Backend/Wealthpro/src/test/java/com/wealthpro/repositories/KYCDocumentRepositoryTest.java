package com.wealthpro.repositories;

import com.wealthpro.entities.Client;
import com.wealthpro.entities.KYCDocument;
import com.wealthpro.enums.ClientSegment;
import com.wealthpro.enums.ClientStatus;
import com.wealthpro.enums.KycStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class KYCDocumentRepositoryTest {

    @Autowired
    private KYCDocumentRepository kycDocumentRepository;

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

    @AfterEach
    void clearall(){
        savedClient=null;
    }

    // ─────────────────────────────────────────
    // TEST 1: Save KYC document
    // ─────────────────────────────────────────
    @Test
    void testSaveKYCDocument_Success() {
        KYCDocument kyc = new KYCDocument();
        kyc.setClient(savedClient);
        kyc.setDocumentType("PAN");
        kyc.setDocumentRef("http://cloudinary.com/kyc/pan_123.jpg");
        kyc.setStatus(KycStatus.Pending);

        KYCDocument saved = kycDocumentRepository.save(kyc);

        assertNotNull(saved);
        assertNotNull(saved.getKycId());
        assertEquals("PAN", saved.getDocumentType());
        assertEquals(KycStatus.Pending, saved.getStatus());
    }

    // ─────────────────────────────────────────
    // TEST 2: Find KYC documents by client ID
    // ─────────────────────────────────────────
    @Test
    void testFindByClientClientId_ReturnsDocuments() {
        KYCDocument kyc1 = new KYCDocument();
        kyc1.setClient(savedClient);
        kyc1.setDocumentType("PAN");
        kyc1.setDocumentRef("http://cloudinary.com/pan.jpg");
        kyc1.setStatus(KycStatus.Pending);
        kycDocumentRepository.save(kyc1);

        KYCDocument kyc2 = new KYCDocument();
        kyc2.setClient(savedClient);
        kyc2.setDocumentType("Aadhaar");
        kyc2.setDocumentRef("http://cloudinary.com/aadhaar.jpg");
        kyc2.setStatus(KycStatus.Pending);
        kycDocumentRepository.save(kyc2);

        List<KYCDocument> docs = kycDocumentRepository
                .findByClientClientId(savedClient.getClientId());

        assertEquals(2, docs.size());
        assertEquals(savedClient.getClientId(),
                docs.get(0).getClient().getClientId());
    }

    // ─────────────────────────────────────────
    // TEST 3: No KYC documents for client
    // ─────────────────────────────────────────
    @Test
    void testFindByClientClientId_WhenNoDocuments_ReturnsEmpty() {
        List<KYCDocument> docs = kycDocumentRepository
                .findByClientClientId(savedClient.getClientId());

        assertEquals(0, docs.size());
    }

    // ─────────────────────────────────────────
    // TEST 4: Update KYC status to Verified
    // ─────────────────────────────────────────
    @Test
    void testUpdateKYCStatus_ToVerified() {
        KYCDocument kyc = new KYCDocument();
        kyc.setClient(savedClient);
        kyc.setDocumentType("PAN");
        kyc.setDocumentRef("http://cloudinary.com/pan.jpg");
        kyc.setStatus(KycStatus.Pending);
        KYCDocument saved = kycDocumentRepository.save(kyc);

        saved.setStatus(KycStatus.Verified);
        saved.setVerifiedDate(LocalDate.now());
        KYCDocument updated = kycDocumentRepository.save(saved);

        assertEquals(KycStatus.Verified, updated.getStatus());
        assertEquals(LocalDate.now(), updated.getVerifiedDate());
    }

    // ─────────────────────────────────────────
    // TEST 5: Delete KYC document
    // ─────────────────────────────────────────
    @Test
    void testDeleteKYCDocument_Success() {
        KYCDocument kyc = new KYCDocument();
        kyc.setClient(savedClient);
        kyc.setDocumentType("PAN");
        kyc.setDocumentRef("http://cloudinary.com/pan.jpg");
        kyc.setStatus(KycStatus.Pending);
        KYCDocument saved = kycDocumentRepository.save(kyc);

        kycDocumentRepository.deleteById(saved.getKycId());

        Optional<KYCDocument> found = kycDocumentRepository
                .findById(saved.getKycId());
        assertFalse(found.isPresent());
    }
}