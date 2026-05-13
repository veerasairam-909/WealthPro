package wealthpro.springbootapigateway.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import wealthpro.springbootapigateway.entities.AuditUsers;
import wealthpro.springbootapigateway.repository.AuditUsersRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditControllerTest {

    @Mock
    private AuditUsersRepository repo;

    @InjectMocks
    private AuditController auditController;

    // helper to build audit entries
    private AuditUsers buildAudit(long id, String username, String method, LocalDateTime ts) {
        AuditUsers a = new AuditUsers();
        a.setId(id);
        a.setUsername(username);
        a.setMethod(method);
        a.setTimestamp(ts);
        return a;
    }

    // ─── list — no filters ────────────────────────────────────────────────────

    @Test
    void testList_NoFilters_ReturnsAllEntries() {
        LocalDateTime now = LocalDateTime.now();
        AuditUsers a1 = buildAudit(1L, "user1", "GET",  now.minusMinutes(5));
        AuditUsers a2 = buildAudit(2L, "user2", "POST", now.minusMinutes(1));

        when(repo.findAll()).thenReturn(Flux.fromIterable(List.of(a1, a2)));

        Flux<AuditUsers> result = auditController.list(null, null, null, null, 200);

        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void testList_EmptyRepo_ReturnsEmpty() {
        when(repo.findAll()).thenReturn(Flux.empty());

        Flux<AuditUsers> result = auditController.list(null, null, null, null, 200);

        StepVerifier.create(result)
                .verifyComplete();
    }

    // ─── list — filter by username ────────────────────────────────────────────

    @Test
    void testList_FilterByUsername_OnlyMatchingReturned() {
        LocalDateTime now = LocalDateTime.now();
        AuditUsers a1 = buildAudit(1L, "rm_john",     "GET",  now.minusMinutes(3));
        AuditUsers a2 = buildAudit(2L, "dealer_jane", "POST", now.minusMinutes(2));
        AuditUsers a3 = buildAudit(3L, "rm_john",     "PUT",  now.minusMinutes(1));

        when(repo.findAll()).thenReturn(Flux.fromIterable(List.of(a1, a2, a3)));

        Flux<AuditUsers> result = auditController.list("rm_john", null, null, null, 200);

        // only the 2 rm_john entries should come back
        StepVerifier.create(result)
                .assertNext(a -> assertEquals("rm_john", a.getUsername()))
                .assertNext(a -> assertEquals("rm_john", a.getUsername()))
                .verifyComplete();
    }

    // ─── list — filter by method ──────────────────────────────────────────────

    @Test
    void testList_FilterByMethod_OnlyMatchingReturned() {
        LocalDateTime now = LocalDateTime.now();
        AuditUsers a1 = buildAudit(1L, "user1", "GET",    now.minusMinutes(4));
        AuditUsers a2 = buildAudit(2L, "user2", "POST",   now.minusMinutes(3));
        AuditUsers a3 = buildAudit(3L, "user3", "DELETE", now.minusMinutes(2));

        when(repo.findAll()).thenReturn(Flux.fromIterable(List.of(a1, a2, a3)));

        Flux<AuditUsers> result = auditController.list(null, "POST", null, null, 200);

        StepVerifier.create(result)
                .assertNext(a -> assertEquals("POST", a.getMethod()))
                .verifyComplete();
    }

    // ─── list — limit ─────────────────────────────────────────────────────────

    @Test
    void testList_LimitIsRespected() {
        LocalDateTime now = LocalDateTime.now();
        List<AuditUsers> entries = List.of(
                buildAudit(1L, "u1", "GET", now.minusMinutes(5)),
                buildAudit(2L, "u2", "GET", now.minusMinutes(4)),
                buildAudit(3L, "u3", "GET", now.minusMinutes(3)),
                buildAudit(4L, "u4", "GET", now.minusMinutes(2)),
                buildAudit(5L, "u5", "GET", now.minusMinutes(1))
        );

        when(repo.findAll()).thenReturn(Flux.fromIterable(entries));

        // only 3 entries should come back
        Flux<AuditUsers> result = auditController.list(null, null, null, null, 3);

        StepVerifier.create(result)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void testList_LimitZero_CapToMinimumOne() {
        // passing limit=0 should be capped to 1 by Math.max(limit, 1)
        LocalDateTime now = LocalDateTime.now();
        AuditUsers a1 = buildAudit(1L, "u1", "GET", now.minusMinutes(2));
        AuditUsers a2 = buildAudit(2L, "u2", "GET", now.minusMinutes(1));

        when(repo.findAll()).thenReturn(Flux.fromIterable(List.of(a1, a2)));

        Flux<AuditUsers> result = auditController.list(null, null, null, null, 0);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    // ─── getById ──────────────────────────────────────────────────────────────

    @Test
    void testGetById_ReturnsSingleEntry() {
        LocalDateTime now = LocalDateTime.now();
        AuditUsers a = buildAudit(42L, "admin", "DELETE", now);

        when(repo.findById(42L)).thenReturn(Mono.just(a));

        Mono<AuditUsers> result = auditController.getById(42L);

        StepVerifier.create(result)
                .assertNext(entry -> {
                    assertEquals(42L, entry.getId());
                    assertEquals("admin", entry.getUsername());
                })
                .verifyComplete();
    }

    @Test
    void testGetById_NotFound_ReturnsEmpty() {
        when(repo.findById(999L)).thenReturn(Mono.empty());

        Mono<AuditUsers> result = auditController.getById(999L);

        StepVerifier.create(result)
                .verifyComplete(); // empty, no error
    }

    // ─── list — sorted newest first ───────────────────────────────────────────

    @Test
    void testList_SortedNewestFirst() {
        LocalDateTime now = LocalDateTime.now();
        // add them in oldest-first order
        AuditUsers old   = buildAudit(1L, "u1", "GET", now.minusHours(2));
        AuditUsers mid   = buildAudit(2L, "u2", "GET", now.minusHours(1));
        AuditUsers newest = buildAudit(3L, "u3", "GET", now);

        when(repo.findAll()).thenReturn(Flux.fromIterable(List.of(old, mid, newest)));

        Flux<AuditUsers> result = auditController.list(null, null, null, null, 200);

        // after sort, newest should be first
        StepVerifier.create(result)
                .assertNext(a -> assertEquals(3L, a.getId()))   // newest
                .assertNext(a -> assertEquals(2L, a.getId()))   // mid
                .assertNext(a -> assertEquals(1L, a.getId()))   // oldest
                .verifyComplete();
    }
}
