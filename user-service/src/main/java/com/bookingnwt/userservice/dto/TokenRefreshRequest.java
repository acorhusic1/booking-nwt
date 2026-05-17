package com.bookingnwt.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRefreshRequest {
    @NotBlank(message = "Refresh token je obavezan")
    private String refreshToken;
}
