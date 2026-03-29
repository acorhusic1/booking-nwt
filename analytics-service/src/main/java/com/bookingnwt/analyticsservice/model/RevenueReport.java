package com.bookingnwt.analyticsservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "revenue_report", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"host_id", "year", "month"})
})
@Getter
@Setter
@NoArgsConstructor
public class RevenueReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "host_id", nullable = false)
    private Long hostId;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Column(name = "total_revenue", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "platform_commission", nullable = false, precision = 10, scale = 2)
    private BigDecimal platformCommission;

    @Column(name = "net_revenue", nullable = false, precision = 10, scale = 2)
    private BigDecimal netRevenue;

    @Column(name = "total_reservations", nullable = false)
    private Integer totalReservations;

    @Column(name = "total_cancellations", nullable = false)
    private Integer totalCancellations;

    @Column(name = "total_properties", nullable = false)
    private Integer totalProperties;

    @Column(name = "average_occupancy_rate", precision = 5, scale = 2)
    private BigDecimal averageOccupancyRate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public RevenueReport(Long hostId, Integer year, Integer month,
                         BigDecimal totalRevenue, BigDecimal platformCommission,
                         BigDecimal netRevenue, Integer totalReservations,
                         Integer totalCancellations, Integer totalProperties,
                         BigDecimal averageOccupancyRate) {
        this.hostId = hostId;
        this.year = year;
        this.month = month;
        this.totalRevenue = totalRevenue;
        this.platformCommission = platformCommission;
        this.netRevenue = netRevenue;
        this.totalReservations = totalReservations;
        this.totalCancellations = totalCancellations;
        this.totalProperties = totalProperties;
        this.averageOccupancyRate = averageOccupancyRate;
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
