package com.wealthpro.orderexecution.entities;

import com.wealthpro.orderexecution.enums.OrderStatus;
import com.wealthpro.orderexecution.enums.PriceType;
import com.wealthpro.orderexecution.enums.Side;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a trading order.
 * <p>
 * An order progresses through a lifecycle:
 * PLACED → VALIDATED → ROUTED → PARTIALLY_FILLED → FILLED.
 * It may be REJECTED or CANCELLED at various stages.
 * </p>
 *
 * @author WealthPro Team
 * @version 2.0
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private Long securityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Side side;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriceType priceType;

    private Double limitPrice;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    /** The venue to which this order was routed (set during routing). */
    private String routedVenue;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PreTradeCheck> preTradeChecks = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExecutionFill> executionFills = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Allocation> allocations = new ArrayList<>();

    /** Automatically sets the order date if not already set. */
    @PrePersist
    protected void onCreate() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
    }
}
