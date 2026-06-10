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
public class PropertyResponse {
    private Long id;
    private Long hostId;
    private String name;
    private String description;
    private String address;
    private String city;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer maxGuests;
    private Boolean isActive;
    private Boolean available;
    private LocalDateTime createdAt;
    private String primaryImageUrl;
    // F2 — kucna pravila + moderacija status
    private Boolean ruleNoSmoking;
    private Boolean rulePetsAllowed;
    private Boolean rulePartiesAllowed;
    private Boolean ruleChildrenAllowed;
    private String moderationStatus;
    // F1 — bazna cijena (iz PricingRule) da frontend moze filtrirati/sortirati
    private java.math.BigDecimal basePrice;
    // F1 — sadrzaji (lista imena za frontend filter + display)
    private java.util.List<String> amenities;
    // F1/F2 — tip smjestaja (APARTMAN, KUCA, HOTEL, HOSTEL, VILA...)
    private String propertyType;
    // F11 — broj pregleda oglasa
    private Long viewCount;
}
