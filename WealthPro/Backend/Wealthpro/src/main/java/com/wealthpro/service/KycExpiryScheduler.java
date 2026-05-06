package com.wealthpro.service;

import com.wealthpro.entities.KYCDocument;
import com.wealthpro.enums.KycStatus;
import com.wealthpro.feign.NotificationFeignClient;
import com.wealthpro.feign.dto.NotificationRequestDTO;
import com.wealthpro.repositories.KYCDocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Runs daily and marks KYC documents as Expired when their expiryDate has passed.
 * Sends a notification to the client so they know to re-submit via their RM.
 */
@Slf4j
@Component
public class KycExpiryScheduler {

    private final KYCDocumentRepository kycDocumentRepository;
    private final NotificationFeignClient notificationFeignClient;

    public KycExpiryScheduler(KYCDocumentRepository kycDocumentRepository,
                               NotificationFeignClient notificationFeignClient) {
        this.kycDocumentRepository = kycDocumentRepository;
        this.notificationFeignClient = notificationFeignClient;
    }

    /**
     * Runs every day at 1:00 AM.
     * Finds all Verified documents whose expiryDate is today or in the past,
     * marks them Expired, and sends a notification to the client.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void expireKycDocuments() {
        List<KYCDocument> dueForExpiry = kycDocumentRepository
                .findByStatusAndExpiryDateLessThanEqual(KycStatus.Verified, LocalDate.now());

        if (dueForExpiry.isEmpty()) {
            return;
        }

        log.info("[KYC-EXPIRY] Found {} document(s) to expire", dueForExpiry.size());

        for (KYCDocument doc : dueForExpiry) {
            doc.setStatus(KycStatus.Expired);
            kycDocumentRepository.save(doc);

            try {
                String message = String.format(
                        "Your %s document (KYC ID: %d) has expired on %s. " +
                        "Please contact your Relationship Manager to re-submit your KYC documents.",
                        doc.getDocumentType(), doc.getKycId(), doc.getExpiryDate()
                );
                notificationFeignClient.sendNotification(new NotificationRequestDTO(
                        doc.getClient().getClientId(), message, "Compliance"
                ));
                log.info("[KYC-EXPIRY] Expired KYC {} for client {}", doc.getKycId(),
                        doc.getClient().getClientId());
            } catch (Exception e) {
                // Notification failure must not stop the expiry from being saved
                log.warn("[KYC-EXPIRY] Could not send notification for KYC {}: {}",
                        doc.getKycId(), e.getMessage());
            }
        }

        log.info("[KYC-EXPIRY] Completed — {} document(s) marked Expired", dueForExpiry.size());
    }
}
