package com.bookingnwt.propertyservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyImageRequest {

    @NotBlank(message = "URL slike je obavezan")
    private String url;

    private Boolean isPrimary = false;
}
