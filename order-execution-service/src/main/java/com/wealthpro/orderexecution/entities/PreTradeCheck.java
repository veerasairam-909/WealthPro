package com.wealthpro.orderexecution.entities;

import com.wealthpro.orderexecution.enums.CheckResult;
import com.wealthpro.orderexecution.enums.CheckType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing a pre-trade compliance check result.
 *
 * @author WealthPro Team
 * @version 2.0
 */
@Entity
@Table(name = "pre_trade_checks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreTradeCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long checkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckType checkType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckResult result;

    private String message;

    @Column(nullable = false)
    private LocalDateTime checkedDate;

    @PrePersist
    protected void onCreate() {
        if (checkedDate == null) {
            checkedDate = LocalDateTime.now();
        }
    }
}
