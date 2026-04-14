package com.bookingnwt.reservationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancellationPolicyResponseDTO {

    private Long id;
    private Long propertyId;
    private String name;
    private Integer freeCancelDays;
    private Integer partialRefundPct;
    private Boolean noRefund;
    private LocalDateTime createdAt;
}
