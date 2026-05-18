package com.bookingnwt.userservice.service;

import com.bookingnwt.userservice.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(Long userId);
    RefreshToken verifyExpiration(RefreshToken token);
    void deleteByUserId(Long userId);
    Optional<RefreshToken> findByToken(String token);
}
