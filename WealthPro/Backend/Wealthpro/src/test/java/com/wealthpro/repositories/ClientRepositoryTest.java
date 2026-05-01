package com.wealthpro.repositories;

import com.wealthpro.entities.Client;
import com.wealthpro.enums.ClientSegment;
import com.wealthpro.enums.ClientStatus;
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
public class ClientRepositoryTest {

    @Autowired
    private ClientRepository clientRepository;

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setName("Murali Krishna");
        client.setDob(LocalDate.of(1995, 6, 15));
        client.setContactInfo("murali@gmail.com | 9876543210");
        client.setSegment(ClientSegment.HNI);
        client.setStatus(ClientStatus.Active);
    }
    @AfterEach
    void clearall(){
        client=null;
    }

    // ─────────────────────────────────────────
    // TEST 1: Save client
    // ─────────────────────────────────────────
    @Test
    void testSaveClient_Success() {
        Client saved = clientRepository.save(client);

        assertNotNull(saved);
        assertNotNull(saved.getClientId());                            // ID was auto-generated
        assertEquals("Murali Krishna", saved.getName());
        assertEquals(ClientSegment.HNI, saved.getSegment());
        assertEquals(ClientStatus.Active, saved.getStatus());
    }

    // ─────────────────────────────────────────
    // TEST 2: Find client by ID — exists
    // ─────────────────────────────────────────
    @Test
    void testFindById_WhenClientExists_ReturnsClient() {
        Client saved = clientRepository.save(client);

        Optional<Client> found = clientRepository.findById(saved.getClientId());

        assertTrue(found.isPresent());
        assertEquals("Murali Krishna", found.get().getName());
    }

    // ─────────────────────────────────────────
    // TEST 3: Find client by ID — not exists
    // ─────────────────────────────────────────
    @Test
    void testFindById_WhenClientNotExists_ReturnsEmpty() {
        Optional<Client> found = clientRepository.findById(999L);

        assertFalse(found.isPresent());
    }

    // ─────────────────────────────────────────
    // TEST 4: Find all clients
    // ─────────────────────────────────────────
    @Test
    void testFindAllClients_ReturnsAllSavedClients() {
        clientRepository.save(client);

        Client client2 = new Client();
        client2.setName("Raj Kumar");
        client2.setDob(LocalDate.of(1990, 1, 10));
        client2.setContactInfo("raj@gmail.com");
        client2.setSegment(ClientSegment.Retail);
        client2.setStatus(ClientStatus.Active);
        clientRepository.save(client2);

        List<Client> clients = clientRepository.findAll();

        assertEquals(2, clients.size());
    }

    // ─────────────────────────────────────────
    // TEST 5: Delete client
    // ─────────────────────────────────────────
    @Test
    void testDeleteClient_Success() {
        Client saved = clientRepository.save(client);
        Long id = saved.getClientId();

        clientRepository.deleteById(id);

        Optional<Client> found = clientRepository.findById(id);
        assertFalse(found.isPresent());
    }

    // ─────────────────────────────────────────
    // TEST 6: Update client
    // ─────────────────────────────────────────
    @Test
    void testUpdateClient_Success() {
        Client saved = clientRepository.save(client);

        saved.setName("Murali Krishna R");
        Client updated = clientRepository.save(saved);

        assertEquals("Murali Krishna R", updated.getName());
        assertEquals(saved.getClientId(), updated.getClientId()); // same ID
    }
}