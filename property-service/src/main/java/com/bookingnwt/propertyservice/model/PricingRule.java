package com.bookingnwt.propertyservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_rule")
@Getter
@Setter
@NoArgsConstructor
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false, unique = true)
    private Property property;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "weekend_price")
    private BigDecimal weekendPrice;

    @Column(name = "min_stay_days")
    private Integer minStayDays;

    @Column(name = "max_stay_days")
    private Integer maxStayDays;

    @Column(name = "long_stay_discount_pct")
    private Integer longStayDiscountPct;

    @Column(name = "long_stay_threshold")
    private Integer longStayThreshold;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public PricingRule(Property property, BigDecimal basePrice, BigDecimal weekendPrice,
                       Integer minStayDays, Integer maxStayDays,
                       Integer longStayDiscountPct, Integer longStayThreshold) {
        this.property = property;
        this.basePrice = basePrice;
        this.weekendPrice = weekendPrice;
        this.minStayDays = minStayDays;
        this.maxStayDays = maxStayDays;
        this.longStayDiscountPct = longStayDiscountPct;
        this.longStayThreshold = longStayThreshold;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

}
