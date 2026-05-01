package com.wealthpro.repositories;

import com.wealthpro.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    /** Used by the gateway / other services to resolve username → clientId. */
    Optional<Client> findByUsername(String username);

    boolean existsByUsername(String username);
}
