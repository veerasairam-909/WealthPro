package wealthpro.springbootapigateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import wealthpro.springbootapigateway.entities.AuditUsers;
import wealthpro.springbootapigateway.repository.AuditUsersRepository;

import java.time.LocalDateTime;

/**
 * Admin-only audit log viewer.
 * Lives under /auth/audit so it's grouped with the other ADMIN endpoints.
 */
@RestController
@RequestMapping("/auth/audit")
public class AuditController {

    @Autowired
    private AuditUsersRepository repo;

    /**
     * Returns all audit entries newest first, with optional filters.
     * Query params (all optional):
     *   - username  : filter by exact username
     *   - method    : filter by HTTP method (GET/POST/...)
     *   - from      : ISO date-time, e.g. 2026-01-01T00:00:00
     *   - to        : ISO date-time
     *   - limit     : max rows returned (default 200, max 1000)
     */
    @GetMapping
    public Flux<AuditUsers> list(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "200") int limit) {

        final LocalDateTime fromTs = parse(from);
        final LocalDateTime toTs   = parse(to);
        final int cap              = Math.min(Math.max(limit, 1), 1000);

        return repo.findAll()
                .filter(a -> username == null || username.equalsIgnoreCase(a.getUsername()))
                .filter(a -> method   == null || method.equalsIgnoreCase(a.getMethod()))
                .filter(a -> fromTs   == null || (a.getTimestamp() != null && !a.getTimestamp().isBefore(fromTs)))
                .filter(a -> toTs     == null || (a.getTimestamp() != null && !a.getTimestamp().isAfter(toTs)))
                .sort((x, y) -> y.getTimestamp().compareTo(x.getTimestamp()))   // newest first
                .take(cap);
    }

    /** Single audit entry by id. */
    @GetMapping("/{id}")
    public Mono<AuditUsers> getById(@PathVariable Long id) {
        return repo.findById(id);
    }

    private static LocalDateTime parse(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDateTime.parse(s);
        } catch (Exception e) {
            return null;  // ignore bad input
        }
    }
}
