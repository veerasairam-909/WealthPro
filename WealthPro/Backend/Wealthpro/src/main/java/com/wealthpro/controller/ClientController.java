package com.wealthpro.controller;

import com.wealthpro.dto.request.ClientProvisionRequestDTO;
import com.wealthpro.dto.request.ClientRequestDTO;
import com.wealthpro.dto.response.ClientResponseDTO;
import com.wealthpro.exception.ResourceNotFoundException;
import com.wealthpro.security.AuthContext;
import com.wealthpro.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    // POST /api/clients — RM/COMPLIANCE/ADMIN create full client
    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(
            @Valid @RequestBody ClientRequestDTO requestDTO) {
        ClientResponseDTO response = clientService.createClient(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Internal endpoint: called by the API Gateway after a CLIENT self-registers.
     * Creates a stub Client row (status=PENDING_KYC) linked to the login username.
     * Idempotent — re-posting with the same username returns the existing row.
     *
     * This path is reachable only from inside the cluster (it's under
     * /api/clients/** which the gateway exposes only to RM/COMPLIANCE/ADMIN;
     * but /api/clients/provision is gateway-internal — see SecurityConfig).
     * For belt-and-braces protection, the gateway could call a dedicated
     * internal port, but role-restricted access from the gateway's own filter
     * is sufficient here.
     */
    @PostMapping("/provision")
    public ResponseEntity<ClientResponseDTO> provisionFromSelfRegistration(
            @Valid @RequestBody ClientProvisionRequestDTO request) {
        ClientResponseDTO response = clientService.provisionFromSelfRegistration(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Used by the gateway at login time to resolve username → clientId. */
    @GetMapping("/by-username/{username}")
    public ResponseEntity<ClientResponseDTO> getByUsername(@PathVariable String username) {
        return clientService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No client linked to username: " + username));
    }

    // GET /api/clients
    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    // GET /api/clients/{clientId}
    @GetMapping("/{clientId}")
    public ResponseEntity<ClientResponseDTO> getClientById(
            @PathVariable Long clientId,
            @RequestHeader(value = AuthContext.HDR_ROLES,     required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        AuthContext ctx = new AuthContext(null, roles, authClientId);
        if (ctx.isClient() && !ctx.ownsClient(clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only view your own client profile.");
        }

        return ResponseEntity.ok(clientService.getClientById(clientId));
    }

    // PUT /api/clients/{clientId}
    @PutMapping("/{clientId}")
    public ResponseEntity<ClientResponseDTO> updateClient(
            @PathVariable Long clientId,
            @Valid @RequestBody ClientRequestDTO requestDTO) {
        return ResponseEntity.ok(clientService.updateClient(clientId, requestDTO));
    }

    // DELETE /api/clients/{clientId}
    @DeleteMapping("/{clientId}")
    public ResponseEntity<String> deleteClient(@PathVariable Long clientId) {
        clientService.deleteClient(clientId);
        return ResponseEntity.ok("Client deleted successfully");
    }
}
