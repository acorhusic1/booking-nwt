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
    private String email;
    private String role;
    private Long id;

    public LoginResponse(String accessToken, String email, String role, Long id) {
        this.accessToken = accessToken;
        this.email = email;
        this.role = role;
        this.id = id;
    }
}
