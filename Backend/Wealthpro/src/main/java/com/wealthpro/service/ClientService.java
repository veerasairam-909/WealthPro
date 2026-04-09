package com.wealthpro.service;

import com.wealthpro.dto.request.ClientRequestDTO;
import com.wealthpro.dto.response.ClientResponseDTO;
import com.wealthpro.entities.Client;

import java.util.List;

public interface ClientService {

    ClientResponseDTO createClient(ClientRequestDTO requestDTO);

    ClientResponseDTO getClientById(Long clientId);

    List<ClientResponseDTO> getAllClients();

    ClientResponseDTO updateClient(Long clientId, ClientRequestDTO requestDTO);

    void deleteClient(Long clientId);

    // Used by KYCService and RiskProfileService internally
    Client findClientOrThrow(Long clientId);
}