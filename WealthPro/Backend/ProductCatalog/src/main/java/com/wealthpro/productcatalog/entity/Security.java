package com.wealthpro.productcatalog.entity;

import com.wealthpro.productcatalog.enums.AssetClass;
import com.wealthpro.productcatalog.enums.SecurityStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "securities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Security {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "security_id")
    private Long securityId;

    @Column(name = "symbol", nullable = false, unique = true, length = 20)
    private String symbol;

    @Column(name = "name", length = 150)
    private String name;

    @Column(name = "exchange", length = 20)
    private String exchange;

    @Column(name = "isin", length = 12)
    private String isin;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_class", nullable = false)
    private AssetClass assetClass;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "country", nullable = false, length = 50)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SecurityStatus status;

    /** Last known market price — used for display purposes only. Null for instruments priced at NAV. */
    @Column(name = "current_price", precision = 15, scale = 4)
    private BigDecimal currentPrice;

    @OneToMany(mappedBy = "security", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductTerm> productTerms;

    @OneToMany(mappedBy = "security", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResearchNote> researchNotes;
}