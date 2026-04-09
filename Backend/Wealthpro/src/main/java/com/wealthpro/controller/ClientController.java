package com.wealthpro.controller;

import com.wealthpro.dto.request.ClientRequestDTO;
import com.wealthpro.dto.response.ClientResponseDTO;
import com.wealthpro.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private final ClientService clientService;

    public ClientController(ClientService clientService) {

        this.clientService = clientService;
    }

    // POST /api/clients
    // Create a new client
    // Postman: Body → raw → JSON

    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(
            @Valid @RequestBody ClientRequestDTO requestDTO) {

        ClientResponseDTO response = clientService.createClient(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201
    }

    // GET /api/clients
    // Get all clients

    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients()); // 200
    }

    // GET /api/clients/{clientId}
    // Get client by ID
    @GetMapping("/{clientId}")
    public ResponseEntity<ClientResponseDTO> getClientById(
            @PathVariable Long clientId) {

        return ResponseEntity.ok(clientService.getClientById(clientId)); // 200
    }


    // PUT /api/clients/{clientId}
    // Update client by ID
    // Postman: Body → raw → JSON
    @PutMapping("/{clientId}")
    public ResponseEntity<ClientResponseDTO> updateClient(
            @PathVariable Long clientId,
            @Valid @RequestBody ClientRequestDTO requestDTO) {

        return ResponseEntity.ok(clientService.updateClient(clientId, requestDTO)); // 200
    }

    // DELETE /api/clients/{clientId}
    // Delete client by ID
    @DeleteMapping("/{clientId}")
    public ResponseEntity<String> deleteClient(
            @PathVariable Long clientId) {

        clientService.deleteClient(clientId);
        return ResponseEntity.ok("Client deleted successfully"); // 200
    }
}