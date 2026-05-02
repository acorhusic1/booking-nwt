package com.bookingnwt.reservationservice.dto;

import com.bookingnwt.reservationservice.model.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponseDTO {

    private Long id;
    private Long guestId;
    private Long hostId;
    private Long propertyId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer numGuests;
    private BigDecimal totalPrice;
    private ReservationStatus status;
    private Long cancellationPolicyId;
    private Long promoCodeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
