package com.wealth.pbor.security;

import com.wealth.pbor.entity.Account;
import com.wealth.pbor.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Enforces that a CLIENT user can only access rows they own.
 * Staff roles always pass through.
 *
 * Usage in controllers:
 *    ownershipGuard.checkClient(ctx, clientId);
 *    ownershipGuard.checkAccount(ctx, accountId);
 */
@Component
@RequiredArgsConstructor
public class OwnershipGuard {

    private final AccountRepository accountRepository;

    /** Throws 403 if a CLIENT tries to access a clientId that isn't theirs. */
    public void checkClient(AuthContext ctx, Long requestedClientId) {
        if (ctx.isStaff()) return;
        if (!ctx.isClient() || !ctx.ownsClient(requestedClientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not allowed to access data for client " + requestedClientId + ".");
        }
    }

    /** Throws 403 if a CLIENT tries to access an account that doesn't belong to them. */
    public void checkAccount(AuthContext ctx, Long accountId) {
        if (ctx.isStaff()) return;
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Account not found: " + accountId));
        if (!ctx.isClient() || !ctx.ownsClient(account.getClientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not allowed to access account " + accountId + ".");
        }
    }

    /** Returns the clientId for a given accountId — used by other services' ownership checks. */
    public Long resolveOwner(Long accountId) {
        return accountRepository.findById(accountId)
                .map(Account::getClientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Account not found: " + accountId));
    }
}
