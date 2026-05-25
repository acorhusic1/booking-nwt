package com.bookingnwt.propertyservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ListingRequest {

    @NotNull(message = "Property ID je obavezan")
    private Long propertyId;

    @NotNull(message = "Host ID je obavezan")
    private Long hostId;

    @NotNull(message = "Cijena po noćenju je obavezna")
    private BigDecimal pricePerNight;
}
