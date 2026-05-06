package com.wealthpro.entities;

import com.wealthpro.enums.AmlFlagStatus;
import com.wealthpro.enums.AmlFlagType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "AmlFlag")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AmlFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AmlFlagID")
    @EqualsAndHashCode.Include
    private Long amlFlagId;

    @Column(name = "ClientID", nullable = false)
    private Long clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "FlagType", nullable = false, length = 30)
    private AmlFlagType flagType;

    @Column(name = "Description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private AmlFlagStatus status;

    @Column(name = "RaisedByUserId")
    private Long raisedByUserId;

    @Column(name = "FlaggedDate", nullable = false)
    private LocalDate flaggedDate;

    @Column(name = "ReviewedBy", length = 100)
    private String reviewedBy;

    @Column(name = "ReviewedDate")
    private LocalDate reviewedDate;

    @Column(name = "Notes", columnDefinition = "TEXT")
    private String notes;
}
