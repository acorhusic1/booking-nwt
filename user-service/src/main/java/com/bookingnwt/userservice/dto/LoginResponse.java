package com.bookingnwt.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long id;
    private String email;
    private String role;

    public LoginResponse(String accessToken, Long id, String email, String role) {
        this.accessToken = accessToken;
        this.id = id;
        this.email = email;
        this.role = role;
    }
}
