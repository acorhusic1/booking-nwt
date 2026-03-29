package com.bookingnwt.reservationservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cancellation_policy")
@Getter
@Setter
@NoArgsConstructor
public class CancellationPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(nullable = false)
    private String name;

    @Column(name = "free_cancel_days", nullable = false)
    private Integer freeCancelDays;

    @Column(name = "partial_refund_pct")
    private Integer partialRefundPct;

    @Column(name = "no_refund", nullable = false)
    private Boolean noRefund = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public CancellationPolicy(Long propertyId, String name, Integer freeCancelDays,
                               Integer partialRefundPct, Boolean noRefund) {
        this.propertyId = propertyId;
        this.name = name;
        this.freeCancelDays = freeCancelDays;
        this.partialRefundPct = partialRefundPct;
        this.noRefund = noRefund;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

}
