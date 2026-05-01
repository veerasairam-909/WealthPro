package wealthpro.springbootapigateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;
import wealthpro.springbootapigateway.entities.AuditUsers;
import wealthpro.springbootapigateway.repository.AuditUsersRepository;

@Service
public class AuditService {

    @Autowired
    private AuditUsersRepository repository;

    @Transactional
    public Mono<Void> saveUserAudit(AuditUsers auditUser) {
        return repository.save(auditUser).then();
    }
}
