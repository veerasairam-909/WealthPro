package wealthpro.springbootapigateway.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import wealthpro.springbootapigateway.entities.AuditUsers;
import wealthpro.springbootapigateway.repository.AuditUsersRepository;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @Mock
    private AuditUsersRepository repository;

    @InjectMocks
    private AuditService auditService;

    // helper to build an AuditUsers object
    private AuditUsers buildAudit(String username, String method) {
        AuditUsers audit = new AuditUsers();
        audit.setUsername(username);
        audit.setMethod(method);
        audit.setPath("/api/some/path");
        audit.setTimestamp(LocalDateTime.now());
        return audit;
    }

    // ─── saveUserAudit success ────────────────────────────────────────────────

    @Test
    void testSaveUserAudit_CompletesSuccessfully() {
        AuditUsers audit = buildAudit("rm_john", "GET");

        // repository.save() returns the saved entity
        when(repository.save(any(AuditUsers.class))).thenReturn(Mono.just(audit));

        Mono<Void> result = auditService.saveUserAudit(audit);

        // should complete with no items emitted (Mono<Void>)
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void testSaveUserAudit_RepositoryIsCalledOnce() {
        AuditUsers audit = buildAudit("dealer1", "POST");
        when(repository.save(any(AuditUsers.class))).thenReturn(Mono.just(audit));

        // subscribe to actually trigger the reactive chain
        auditService.saveUserAudit(audit).block();

        verify(repository, times(1)).save(audit);
    }

    // ─── saveUserAudit error propagation ──────────────────────────────────────

    @Test
    void testSaveUserAudit_WhenRepositoryFails_ErrorPropagated() {
        AuditUsers audit = buildAudit("testuser", "DELETE");

        when(repository.save(any(AuditUsers.class)))
                .thenReturn(Mono.error(new RuntimeException("DB connection error")));

        Mono<Void> result = auditService.saveUserAudit(audit);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void testSaveUserAudit_DifferentUsers_EachSavedCorrectly() {
        AuditUsers audit1 = buildAudit("user_a", "GET");
        AuditUsers audit2 = buildAudit("user_b", "POST");

        when(repository.save(any(AuditUsers.class)))
                .thenReturn(Mono.just(audit1))
                .thenReturn(Mono.just(audit2));

        auditService.saveUserAudit(audit1).block();
        auditService.saveUserAudit(audit2).block();

        verify(repository, times(2)).save(any(AuditUsers.class));
    }
}
