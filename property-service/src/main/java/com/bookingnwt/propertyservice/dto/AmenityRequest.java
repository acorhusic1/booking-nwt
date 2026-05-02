package com.bookingnwt.propertyservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AmenityRequest {

    @NotBlank(message = "Naziv sadržaja je obavezan")
    private String name;

    @NotNull(message = "Kategorija je obavezna")
    private String category;
}
