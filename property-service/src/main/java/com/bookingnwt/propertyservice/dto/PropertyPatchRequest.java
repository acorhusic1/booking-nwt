package com.bookingnwt.propertyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO za PATCH (parcijalni update) - sva polja su nullable.
 * Samo polja koja su poslana (non-null) će biti ažurirana.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyPatchRequest {
    private String name;
    private String description;
    private String address;
    private String city;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer maxGuests;
    private String propertyType;
    private Boolean isActive;
    private Boolean available;
}
