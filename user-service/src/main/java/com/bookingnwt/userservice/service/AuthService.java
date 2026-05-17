package com.bookingnwt.userservice.service;

import com.bookingnwt.userservice.dto.LoginRequest;
import com.bookingnwt.userservice.dto.LoginResponse;

import com.bookingnwt.userservice.dto.TokenRefreshRequest;
import com.bookingnwt.userservice.dto.TokenRefreshResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    TokenRefreshResponse refresh(TokenRefreshRequest request);
    void logout(TokenRefreshRequest request);
}
