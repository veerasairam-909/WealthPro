package com.wealthpro.service;

import com.wealthpro.dto.request.ClientRequestDTO;
import com.wealthpro.dto.response.ClientResponseDTO;
import com.wealthpro.entities.Client;
import com.wealthpro.enums.ClientSegment;
import com.wealthpro.enums.ClientStatus;
import com.wealthpro.exception.ResourceNotFoundException;
import com.wealthpro.repositories.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;  // fake repository

    @Mock
    private ModelMapper modelMapper;            // fake modelMapper

    @InjectMocks
    private ClientServiceImpl clientService;    // real service

    private Client client;
    private ClientRequestDTO requestDTO;
    private ClientResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        // Arrange common objects used across tests

        client = new Client();
        client.setClientId(1L);
        client.setName("Murali Krishna");
        client.setDob(LocalDate.of(1995, 6, 15));
        client.setContactInfo("murali@gmail.com");
        client.setSegment(ClientSegment.HNI);
        client.setStatus(ClientStatus.Active);

        requestDTO = new ClientRequestDTO();
        requestDTO.setName("Murali Krishna");
        requestDTO.setDob(LocalDate.of(1995, 6, 15));
        requestDTO.setContactInfo("murali@gmail.com");
        requestDTO.setSegment(ClientSegment.HNI);
        requestDTO.setStatus(ClientStatus.Active);

        responseDTO = new ClientResponseDTO();
        responseDTO.setClientId(1L);
        responseDTO.setName("Murali Krishna");
        responseDTO.setDob(LocalDate.of(1995, 6, 15));
        responseDTO.setContactInfo("murali@gmail.com");
        responseDTO.setSegment(ClientSegment.HNI);
        responseDTO.setStatus(ClientStatus.Active);
    }

    // ─────────────────────────────────────────
    // TEST 1: Create client — success
    // ─────────────────────────────────────────
    @Test
    void testCreateClient_Success() {
        // Arrange
        // when modelMapper maps requestDTO → client entity
        when(modelMapper.map(requestDTO, Client.class)).thenReturn(client);
        // when repository saves → return saved client
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        // when modelMapper maps client → responseDTO
        when(modelMapper.map(client, ClientResponseDTO.class)).thenReturn(responseDTO);

        // Act
        ClientResponseDTO result = clientService.createClient(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Murali Krishna", result.getName());
        assertEquals(ClientStatus.Active, result.getStatus());

        // Verify save was called once
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    // ─────────────────────────────────────────
    // TEST 2: Get client by ID — success
    // ─────────────────────────────────────────
    @Test
    void testGetClientById_Success() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(modelMapper.map(client, ClientResponseDTO.class)).thenReturn(responseDTO);

        // Act
        ClientResponseDTO result = clientService.getClientById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getClientId());
        assertEquals("Murali Krishna", result.getName());

        // Verify findById was called
        verify(clientRepository, times(1)).findById(1L);
    }

    // ─────────────────────────────────────────
    // TEST 3: Get client by ID — not found → exception
    // ─────────────────────────────────────────
    @Test
    void testGetClientById_NotFound_ThrowsException() {
        // Arrange — return empty → simulates client not found in DB
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        // assertThrows verifies the exception is thrown
        assertThrows(ResourceNotFoundException.class, () -> {
            clientService.getClientById(999L);
        });

        verify(clientRepository, times(1)).findById(999L);
    }

    // ─────────────────────────────────────────
    // TEST 4: Get all clients
    // ─────────────────────────────────────────
    @Test
    void testGetAllClients_ReturnsList() {
        // Arrange
        when(clientRepository.findAll()).thenReturn(List.of(client));
        when(modelMapper.map(client, ClientResponseDTO.class)).thenReturn(responseDTO);

        // Act
        List<ClientResponseDTO> result = clientService.getAllClients();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(clientRepository, times(1)).findAll();
    }

    // ─────────────────────────────────────────
    // TEST 5: Update client — success
    // ─────────────────────────────────────────
    @Test
    void testUpdateClient_Success() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(modelMapper.map(client, ClientResponseDTO.class)).thenReturn(responseDTO);

        // Act
        ClientResponseDTO result = clientService.updateClient(1L, requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Murali Krishna", result.getName());

        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    // ─────────────────────────────────────────
    // TEST 6: Update client — not found → exception
    // ─────────────────────────────────────────
    @Test
    void testUpdateClient_NotFound_ThrowsException() {
        // Arrange
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            clientService.updateClient(999L, requestDTO);
        });
    }

    // ─────────────────────────────────────────
    // TEST 7: Delete client — success
    // ─────────────────────────────────────────
    @Test
    void testDeleteClient_Success() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        // Act
        clientService.deleteClient(1L);

        // Assert — verify deleteById was called once with ID 1
        verify(clientRepository, times(1)).deleteById(1L);
    }

    // ─────────────────────────────────────────
    // TEST 8: Delete client — not found → exception
    // ─────────────────────────────────────────
    @Test
    void testDeleteClient_NotFound_ThrowsException() {
        // Arrange
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            clientService.deleteClient(999L);
        });

        // Verify deleteById was NEVER called since client not found
        verify(clientRepository, never()).deleteById(any());
    }
}