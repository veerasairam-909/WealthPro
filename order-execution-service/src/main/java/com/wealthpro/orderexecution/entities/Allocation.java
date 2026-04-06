package com.wealthpro.orderexecution.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing an allocation of a filled order to a client account.
 *
 * @author WealthPro Team
 * @version 2.0
 */
@Entity
@Table(name = "allocations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Allocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long allocationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private Integer allocQuantity;

    @Column(nullable = false)
    private Double allocPrice;

    @Column(nullable = false)
    private LocalDateTime allocDate;

    @PrePersist
    protected void onCreate() {
        if (allocDate == null) {
            allocDate = LocalDateTime.now();
        }
    }
}
