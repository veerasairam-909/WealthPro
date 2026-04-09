package com.wealthpro.service;

import com.wealthpro.dto.request.ClientRequestDTO;
import com.wealthpro.dto.response.ClientResponseDTO;
import com.wealthpro.entities.Client;
import com.wealthpro.enums.ClientStatus;
import com.wealthpro.exception.ResourceNotFoundException;
import com.wealthpro.repositories.ClientRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class ClientServiceImpl implements ClientService{

    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;

    // Constructor injection for injecting the bean
    public ClientServiceImpl(ClientRepository clientRepository, ModelMapper modelMapper) {
        this.clientRepository = clientRepository;
        this.modelMapper = modelMapper;
    }


    // creation of a new client
    public ClientResponseDTO createClient(ClientRequestDTO requestDTO) {


        Client client = modelMapper.map(requestDTO, Client.class);

        // always set status to Active on creation
        client.setStatus(ClientStatus.Active);
        Client saved = clientRepository.save(client);
        return mapToResponse(saved);
    }


    // GET client by ID

    public ClientResponseDTO getClientById(Long clientId) {
        Client client = findClientOrThrow(clientId);
        return mapToResponse(client);
    }


    // Get all clients

    public List<ClientResponseDTO> getAllClients() {
        List<Client> allClients = clientRepository.findAll();
        List<ClientResponseDTO> result = new ArrayList<>();
        for (Client c : allClients) {
            result.add(mapToResponse(c));
        }
        return result;
    }

    // UPDATE client by ID

    public ClientResponseDTO updateClient(Long clientId, ClientRequestDTO requestDTO) {

        // First check client exists
        Client existing = findClientOrThrow(clientId);

        // Update only the fields — do NOT overwrite the ID
        existing.setName(requestDTO.getName());
        existing.setDob(requestDTO.getDob());
        existing.setContactInfo(requestDTO.getContactInfo());
        existing.setSegment(requestDTO.getSegment());
        existing.setStatus(requestDTO.getStatus());

        Client updated = clientRepository.save(existing);
        return mapToResponse(updated);
    }

    // method to delete the client by id in the DB
    public void deleteClient(Long clientId) {


        // Checking exists before deleting
        findClientOrThrow(clientId);
        clientRepository.deleteById(clientId);
    }

    // helper method to find the client or else it will throw an error
    public Client findClientOrThrow(Long clientId) {

        Optional<Client> optional = clientRepository.findById(clientId);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new ResourceNotFoundException("Client not found with ID: " + clientId);
        }

    }

    //this is a helper method to map the client obj to the DTO object.
    private ClientResponseDTO mapToResponse(Client client) {

        return modelMapper.map(client, ClientResponseDTO.class);
    }
}
