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
public class ListingResponse {
    private Long id;
    private Long propertyId;
    private Long hostId;
    private BigDecimal pricePerNight;
    private Boolean isCancelled;
    private LocalDateTime createdAt;
}
