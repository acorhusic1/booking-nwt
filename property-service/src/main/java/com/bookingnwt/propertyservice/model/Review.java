package com.bookingnwt.propertyservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", nullable = false, unique = true)
    private Long reservationId;

    @Column(name = "guest_id", nullable = false)
    private Long guestId;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(name = "host_id", nullable = false)
    private Long hostId;

    @Column(name = "rating_cleanliness", nullable = false)
    private BigDecimal ratingCleanliness;

    @Column(name = "rating_location", nullable = false)
    private BigDecimal ratingLocation;

    @Column(name = "rating_communication", nullable = false)
    private BigDecimal ratingCommunication;

    @Column(name = "rating_value", nullable = false)
    private BigDecimal ratingValue;

    @Column(name = "rating_accuracy", nullable = false)
    private BigDecimal ratingAccuracy;

    @Column(name = "overall_rating", nullable = false)
    private BigDecimal overallRating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "host_reply", columnDefinition = "TEXT")
    private String hostReply;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    public Review(Long reservationId, Long guestId, Long propertyId, Long hostId,
                  BigDecimal ratingCleanliness, BigDecimal ratingLocation,
                  BigDecimal ratingCommunication, BigDecimal ratingValue,
                  BigDecimal ratingAccuracy, String comment) {
        this.reservationId = reservationId;
        this.guestId = guestId;
        this.propertyId = propertyId;
        this.hostId = hostId;
        this.ratingCleanliness = ratingCleanliness;
        this.ratingLocation = ratingLocation;
        this.ratingCommunication = ratingCommunication;
        this.ratingValue = ratingValue;
        this.ratingAccuracy = ratingAccuracy;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
        // Automatski izračunaj overall rating
        this.overallRating = ratingCleanliness.add(ratingLocation)
                .add(ratingCommunication).add(ratingValue).add(ratingAccuracy)
                .divide(new BigDecimal("5"), 2, java.math.RoundingMode.HALF_UP);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

}
