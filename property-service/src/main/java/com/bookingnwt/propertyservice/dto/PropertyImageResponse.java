package com.bookingnwt.propertyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyImageResponse {
    private Long id;
    private Long propertyId;
    private String url;
    private Boolean isPrimary;
    private LocalDateTime uploadedAt;
}
