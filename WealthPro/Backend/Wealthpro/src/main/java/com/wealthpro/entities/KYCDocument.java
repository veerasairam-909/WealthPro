package com.wealthpro.entities;

import com.wealthpro.enums.KycStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "KYCDocument")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "client")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class KYCDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "KYCID")
    @EqualsAndHashCode.Include
    private Long kycId;


    // The relationship is  @ManyToOne .
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "DocumentType", nullable = false, length = 50)
    private String documentType;

    // DocumentRef = the reference/ID number on the document (e.g. PAN number, Aadhaar number)
    @Column(name = "DocumentRef", nullable = false, length = 500)
    private String documentRef;

    @Column(name = "DocumentRefNumber", length = 100)
    private String documentRefNumber;


    //it can hold the null value because if document is not verified then it is null
    @Column(name = "VerifiedDate")
    private LocalDate verifiedDate;

    // Set to verifiedDate + 1 year when document is verified.
    // Null for Pending documents. Scheduler marks document Expired when this date passes.
    @Column(name = "ExpiryDate")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 10)
    private KycStatus status;
}