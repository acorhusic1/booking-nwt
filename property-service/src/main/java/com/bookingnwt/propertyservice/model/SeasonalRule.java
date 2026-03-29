package com.bookingnwt.propertyservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "seasonal_rule")
@Getter
@Setter
@NoArgsConstructor
public class SeasonalRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "price_modifier_pct", nullable = false)
    private Integer priceModifierPct;

    @Column(name = "min_nights")
    private Integer minNights;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public SeasonalRule(Property property, String name, LocalDate startDate, LocalDate endDate,
                        Integer priceModifierPct, Integer minNights) {
        this.property = property;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.priceModifierPct = priceModifierPct;
        this.minNights = minNights;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

}
