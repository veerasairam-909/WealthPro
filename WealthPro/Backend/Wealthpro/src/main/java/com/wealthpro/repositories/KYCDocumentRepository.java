package com.wealthpro.repositories;

import com.wealthpro.entities.KYCDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KYCDocumentRepository extends JpaRepository<KYCDocument, Long> {

    // Spring Data JPA auto-generates the SQL for this:
    // SELECT * FROM KYCDocument WHERE client_id = ?
    // "client" = field name in KYCDocument entity
    // "ClientId" = field name in Client entity
    List<KYCDocument> findByClientClientId(Long clientId);
}