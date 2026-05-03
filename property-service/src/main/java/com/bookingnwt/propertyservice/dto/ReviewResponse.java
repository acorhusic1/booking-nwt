package com.bookingnwt.propertyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long reservationId;
    private Long guestId;
    private String guestName;
    private Long propertyId;
    private Long hostId;
    private BigDecimal ratingCleanliness;
    private BigDecimal ratingLocation;
    private BigDecimal ratingCommunication;
    private BigDecimal ratingValue;
    private BigDecimal ratingAccuracy;
    private BigDecimal overallRating;
    private String comment;
    private String hostReply;
    private LocalDateTime createdAt;
    private LocalDateTime repliedAt;
}
