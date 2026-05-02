package com.bookingnwt.analyticsservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "property_statistics", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"property_id", "`year`", "`month`"})
})
@Getter
@Setter
@NoArgsConstructor
public class PropertyStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(name = "host_id", nullable = false)
    private Long hostId;

    @Column(name = "`year`", nullable = false)
    private Integer year;

    @Column(name = "`month`", nullable = false)
    private Integer month;

    @Column(name = "total_reservations", nullable = false)
    private Integer totalReservations;

    @Column(name = "total_revenue", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "occupancy_rate", precision = 5, scale = 2)
    private BigDecimal occupancyRate;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "cancellation_count", nullable = false)
    private Integer cancellationCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PropertyStatistics(Long propertyId, Long hostId, Integer year, Integer month,
                              Integer totalReservations, BigDecimal totalRevenue,
                              BigDecimal averageRating, BigDecimal occupancyRate,
                              Integer viewCount, Integer cancellationCount) {
        this.propertyId = propertyId;
        this.hostId = hostId;
        this.year = year;
        this.month = month;
        this.totalReservations = totalReservations;
        this.totalRevenue = totalRevenue;
        this.averageRating = averageRating;
        this.occupancyRate = occupancyRate;
        this.viewCount = viewCount;
        this.cancellationCount = cancellationCount;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
