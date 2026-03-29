package com.bookingnwt.reservationservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "problem_report")
@Getter
@Setter
@NoArgsConstructor
public class ProblemReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(nullable = false)
    private String category;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemReportStatus status;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public ProblemReport(Reservation reservation, Long reporterId, String category, String description) {
        this.reservation = reservation;
        this.reporterId = reporterId;
        this.category = category;
        this.description = description;
        this.status = ProblemReportStatus.REPORTED;
        this.reportedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (reportedAt == null) reportedAt = LocalDateTime.now();
    }

}
