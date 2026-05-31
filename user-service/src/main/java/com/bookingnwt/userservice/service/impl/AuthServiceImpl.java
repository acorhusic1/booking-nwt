package com.bookingnwt.userservice.service.impl;

import com.bookingnwt.userservice.dto.LoginRequest;
import com.bookingnwt.userservice.dto.LoginResponse;
import com.bookingnwt.userservice.dto.TokenRefreshRequest;
import com.bookingnwt.userservice.dto.TokenRefreshResponse;
import com.bookingnwt.userservice.exception.TokenRefreshException;
import com.bookingnwt.userservice.model.RefreshToken;
import com.bookingnwt.userservice.model.User;
import com.bookingnwt.userservice.repository.UserRepository;
import com.bookingnwt.userservice.security.JwtProvider;
import com.bookingnwt.userservice.service.AuthService;
import com.bookingnwt.userservice.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
        // Token nosi uid claim — downstream servisi ga koriste umjesto guestId iz body-a.
        // VAZNO: roles moraju imati ROLE_ prefix da bi hasRole('ADMIN') prosao u JwtFilter-u.
        String jwt = jwtProvider.generateToken(
                user.getEmail(),
                java.util.List.of("ROLE_" + user.getRole().name()),
                user.getId()
        );

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new LoginResponse(jwt, refreshToken.getToken(), user.getEmail(), user.getRole().name(), user.getId());
    }

    @Override
    @Transactional
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtProvider.generateToken(user.getEmail(), java.util.List.of("ROLE_" + user.getRole().name()), user.getId());
                    return new TokenRefreshResponse(token, requestRefreshToken);
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token ne postoji u bazi podataka!"));
    }

    @Override
    @Transactional
    public void logout(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        refreshTokenService.findByToken(requestRefreshToken)
                .ifPresent(token -> refreshTokenService.deleteByUserId(token.getUser().getId()));
    }
}
