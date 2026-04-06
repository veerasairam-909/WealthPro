package com.wealth.pbor.entity;

import com.wealth.pbor.enums.CAType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Entity
@Table(name = "corporate_action")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CorporateAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ca_id")
    private Long caId;
    @Column(name = "security_id", nullable = false)
    private Long securityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ca_type", nullable = false, length = 20)
    private CAType caType;
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;
    @Column(name = "ex_date", nullable = false)
    private LocalDate exDate;
    @Column(name = "pay_date", nullable = false)
    private LocalDate payDate;
    @Column(name = "terms_json", columnDefinition = "TEXT")
    private String termsJson;
}