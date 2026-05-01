package com.wealthpro.service;

import com.wealthpro.dto.request.ClientProvisionRequestDTO;
import com.wealthpro.dto.request.ClientRequestDTO;
import com.wealthpro.dto.response.ClientResponseDTO;
import com.wealthpro.entities.Client;

import java.util.List;
import java.util.Optional;

public interface ClientService {

    ClientResponseDTO createClient(ClientRequestDTO requestDTO);

    ClientResponseDTO getClientById(Long clientId);

    List<ClientResponseDTO> getAllClients();

    ClientResponseDTO updateClient(Long clientId, ClientRequestDTO requestDTO);

    void deleteClient(Long clientId);

    // Used by KYCService and RiskProfileService internally
    Client findClientOrThrow(Long clientId);

    /** Gateway-only: stub-create a Client from self-registration payload. */
    ClientResponseDTO provisionFromSelfRegistration(ClientProvisionRequestDTO request);

    /** Used by gateway + other services to resolve username → client. */
    Optional<ClientResponseDTO> findByUsername(String username);
}