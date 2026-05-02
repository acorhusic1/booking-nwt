package com.bookingnwt.userservice.dto;

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
public class IdentityVerificationRequest {

    @NotNull(message = "User ID je obavezan")
    private Long userId;

    @NotBlank(message = "Tip dokumenta je obavezan")
    private String documentType;

    @NotBlank(message = "Broj dokumenta je obavezan")
    private String documentNumber;
}
