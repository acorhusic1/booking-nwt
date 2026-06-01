package com.bookingnwt.propertyservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyRequest {

    @NotNull(message = "Host ID je obavezan")
    private Long hostId;

    @NotBlank(message = "Naziv je obavezan")
    private String name;

    private String description;

    @NotBlank(message = "Adresa je obavezna")
    private String address;

    @NotBlank(message = "Grad je obavezan")
    private String city;

    @NotBlank(message = "Država je obavezna")
    private String country;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer maxGuests;

    // F2 — kucna pravila (opciono, defaultovi se primjenjuju u entity-ju)
    private Boolean ruleNoSmoking;
    private Boolean rulePetsAllowed;
    private Boolean rulePartiesAllowed;
    private Boolean ruleChildrenAllowed;

    // F1 — sadrzaji (amenityId-evi koje korisnik bira pri kreiranju)
    private java.util.Set<Long> amenityIds;
}
