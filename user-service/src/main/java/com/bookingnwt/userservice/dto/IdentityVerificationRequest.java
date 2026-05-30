package com.bookingnwt.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdentityVerificationRequest {

    // userId se setuje iz @PathVariable u kontroleru — ne validira se u body
    private Long userId;

    @NotBlank(message = "Tip dokumenta je obavezan")
    private String documentType;

    @NotBlank(message = "Broj dokumenta je obavezan")
    private String documentNumber;
}
