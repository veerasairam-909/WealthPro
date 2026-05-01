package com.wealthpro.entities;

import com.wealthpro.enums.ClientSegment;
import com.wealthpro.enums.ClientStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Client")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = {"riskProfile", "kycDocuments"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ClientID")
    @EqualsAndHashCode.Include
    private Long clientId;

    /**
     * Link to gateway_db.users.username. NULL for clients created by an RM
     * who have not registered a login yet. UNIQUE so one login = one Client.
     */
    @Column(name = "Username", length = 100, unique = true)
    private String username;

    @Column(name = "Name", nullable = false, length = 150)
    private String name;

    // Nullable — for PENDING_KYC stubs created on self-registration
    // (real DOB is captured later by the RM during KYC).
    @Column(name = "DOB")
    private LocalDate dob;

    @Column(name = "ContactInfo", columnDefinition = "TEXT")
    private String contactInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "Segment", nullable = false, length = 10)
    private ClientSegment segment;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 10)
    private ClientStatus status;

    // One client has one RiskProfile

    @OneToOne(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private RiskProfile riskProfile;

    // One client has many KYC documents

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<KYCDocument> kycDocuments = new ArrayList<>();
}