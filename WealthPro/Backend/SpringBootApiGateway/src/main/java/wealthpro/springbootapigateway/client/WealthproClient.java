package wealthpro.springbootapigateway.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * Reactive client used by the gateway to talk to the Wealthpro (KYC) service.
 *
 * Used for:
 *   - Auto-provisioning a stub Client record when a CLIENT self-registers.
 *   - Resolving username → clientId at login so we can bake it into the JWT.
 */
@Component
public class WealthproClient {

    private static final String SERVICE = "http://WEALTHPRO-SERVICE";

    @Autowired
    private WebClient.Builder webClientBuilder;

    /**
     * Create a stub Client record with status PENDING_KYC.
     * Called right after a successful CLIENT self-registration.
     * Never throws — on failure we log and swallow so the user registration
     * still succeeds. An RM can manually link the user to a Client later.
     */
    public Mono<Long> provisionStubClient(String username, String name,
                                          String email, String phone) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("name",     name);
        body.put("email",    email);
        body.put("phone",    phone);

        return webClientBuilder.build()
                .post()
                .uri(SERVICE + "/api/clients/provision")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(StubClientResponse.class)
                .map(StubClientResponse::getClientId)
                .onErrorResume(e -> {
                    // Log and swallow — registration should not fail because of this
                    System.err.println("[WealthproClient] Stub provisioning failed for '"
                            + username + "': " + e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Look up the clientId linked to a given username (set at self-registration time).
     * Returns empty Mono for staff users (no Client record), 404s, or any error.
     */
    public Mono<Long> resolveClientIdByUsername(String username) {
        return webClientBuilder.build()
                .get()
                .uri(SERVICE + "/api/clients/by-username/{username}", username)
                .retrieve()
                .bodyToMono(StubClientResponse.class)
                .map(StubClientResponse::getClientId)
                .onErrorResume(e -> Mono.empty());
    }

    // Minimal DTO for the response — only clientId is needed here.
    public static class StubClientResponse {
        private Long clientId;
        public Long getClientId() { return clientId; }
        public void setClientId(Long clientId) { this.clientId = clientId; }
    }
}
