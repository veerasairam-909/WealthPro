package wealthpro.springbootapigateway.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import wealthpro.springbootapigateway.entities.AuditUsers;

@Repository
public interface AuditUsersRepository extends ReactiveCrudRepository<AuditUsers, Long> {
}
