package wealthpro.springbootapigateway.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import wealthpro.springbootapigateway.entities.Users;

import reactor.core.publisher.Mono;

@Repository
public interface UsersRepository extends ReactiveCrudRepository<Users, String> {

    Mono<Users> findByUsername(String username);
}
