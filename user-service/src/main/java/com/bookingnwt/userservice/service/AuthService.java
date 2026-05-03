package com.bookingnwt.userservice.service;

import com.bookingnwt.userservice.dto.LoginRequest;
import com.bookingnwt.userservice.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
}
