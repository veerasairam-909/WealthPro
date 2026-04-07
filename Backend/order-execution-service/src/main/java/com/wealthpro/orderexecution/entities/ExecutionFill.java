package com.wealthpro.orderexecution.entities;

import com.wealthpro.orderexecution.enums.FillStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing an execution fill against an order.
 *
 * @author WealthPro Team
 * @version 2.0
 */
@Entity
@Table(name = "execution_fills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionFill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Integer fillQuantity;

    @Column(nullable = false)
    private Double fillPrice;

    @Column(nullable = false)
    private LocalDateTime fillDate;

    private String venue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FillStatus status;

    @PrePersist
    protected void onCreate() {
        if (fillDate == null) {
            fillDate = LocalDateTime.now();
        }
    }
}
