package com.reviewservice.security;

import com.reviewservice.feign.PborAccountClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class OwnershipGuard {

    @Autowired
    private PborAccountClient pborAccountClient;

    public void checkAccount(AuthContext ctx, Long accountId) {
        if (ctx.isStaff()) return;
        if (!ctx.isClient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorised.");
        }
        try {
            Long owner = pborAccountClient.getAccountOwner(accountId);
            if (!ctx.ownsClient(owner)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You are not allowed to access data for account " + accountId + ".");
            }
        } catch (ResponseStatusException e) { throw e; }
          catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Could not verify account ownership for " + accountId + ".");
        }
    }
}
