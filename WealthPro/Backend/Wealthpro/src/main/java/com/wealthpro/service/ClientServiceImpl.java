package com.wealthpro.service;

import com.wealthpro.dto.request.ClientProvisionRequestDTO;
import com.wealthpro.dto.request.ClientRequestDTO;
import com.wealthpro.dto.response.ClientResponseDTO;
import com.wealthpro.entities.Client;
import com.wealthpro.enums.ClientSegment;
import com.wealthpro.enums.ClientStatus;
import com.wealthpro.exception.ResourceNotFoundException;
import com.wealthpro.feign.NotificationFeignClient;
import com.wealthpro.feign.dto.NotificationRequestDTO;
import com.wealthpro.repositories.ClientRepository;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;
    private final NotificationFeignClient notificationFeignClient;

    public ClientServiceImpl(ClientRepository clientRepository,
                             ModelMapper modelMapper,
                             NotificationFeignClient notificationFeignClient) {
        this.clientRepository = clientRepository;
        this.modelMapper = modelMapper;
        this.notificationFeignClient = notificationFeignClient;
    }

    @Override
    public ClientResponseDTO createClient(ClientRequestDTO requestDTO) {
        Client client = modelMapper.map(requestDTO, Client.class);
        client.setStatus(ClientStatus.Active);
        Client saved = clientRepository.save(client);

        // ── Feign: Send welcome notification to Notifications service ────────
        try {
            NotificationRequestDTO notification = new NotificationRequestDTO(
                    saved.getClientId(),
                    "Welcome to WealthPro, " + saved.getName() + "! Your client profile has been created successfully.",
                    "Order"
            );
            notificationFeignClient.sendNotification(notification);
            log.info("[FEIGN] Welcome notification sent to NOTIFICATIONS-SERVICE for clientId={}", saved.getClientId());
        } catch (FeignException e) {
            log.warn("[FEIGN] Could not send notification for clientId={}: {}", saved.getClientId(), e.getMessage());
        }
        // ────────────────────────────────────────────────────────────────────

        return mapToResponse(saved);
    }

    @Override
    public ClientResponseDTO getClientById(Long clientId) {
        Client client = findClientOrThrow(clientId);
        return mapToResponse(client);
    }

    @Override
    public List<ClientResponseDTO> getAllClients() {
        List<Client> allClients = clientRepository.findAll();
        List<ClientResponseDTO> result = new ArrayList<>();
        for (Client c : allClients) {
            result.add(mapToResponse(c));
        }
        return result;
    }

    @Override
    public ClientResponseDTO updateClient(Long clientId, ClientRequestDTO requestDTO) {
        Client existing = findClientOrThrow(clientId);
        existing.setName(requestDTO.getName());
        existing.setDob(requestDTO.getDob());
        existing.setContactInfo(requestDTO.getContactInfo());
        existing.setSegment(requestDTO.getSegment());
        existing.setStatus(requestDTO.getStatus());
        Client updated = clientRepository.save(existing);
        return mapToResponse(updated);
    }

    @Override
    public void deleteClient(Long clientId) {
        findClientOrThrow(clientId);
        clientRepository.deleteById(clientId);
    }

    public Client findClientOrThrow(Long clientId) {
        Optional<Client> optional = clientRepository.findById(clientId);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new ResourceNotFoundException("Client not found with ID: " + clientId);
        }
    }

    // ── Gateway integration ───────────────────────────────────────────────────

    @Override
    public ClientResponseDTO provisionFromSelfRegistration(ClientProvisionRequestDTO request) {
        // Idempotent — if a row for this username already exists, return it.
        Optional<Client> existing = clientRepository.findByUsername(request.getUsername());
        if (existing.isPresent()) {
            return mapToResponse(existing.get());
        }

        Client stub = new Client();
        stub.setUsername(request.getUsername());
        stub.setName(request.getName());
        // Pack email + phone into free-text ContactInfo for now; KYC update fills details.
        String contact = (request.getEmail() != null ? request.getEmail() : "")
                + (request.getPhone() != null ? " | " + request.getPhone() : "");
        stub.setContactInfo(contact.isBlank() ? null : contact);
        stub.setSegment(ClientSegment.Retail);   // default; RM can upgrade to HNI/UHNI
        stub.setStatus(ClientStatus.PENDING_KYC);
        // dob stays null until RM completes KYC

        Client saved = clientRepository.save(stub);
        log.info("[PROVISION] Stub client created for username='{}' clientId={}",
                saved.getUsername(), saved.getClientId());
        return mapToResponse(saved);
    }

    @Override
    public Optional<ClientResponseDTO> findByUsername(String username) {
        return clientRepository.findByUsername(username).map(this::mapToResponse);
    }

    private ClientResponseDTO mapToResponse(Client client) {
        return modelMapper.map(client, ClientResponseDTO.class);
    }
}
