package com.bookingnwt.userservice.service.impl;

import com.bookingnwt.userservice.dto.LoginRequest;
import com.bookingnwt.userservice.dto.LoginResponse;
import com.bookingnwt.userservice.model.User;
import com.bookingnwt.userservice.repository.UserRepository;
import com.bookingnwt.userservice.security.JwtProvider;
import com.bookingnwt.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateToken(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();

        return new LoginResponse(jwt, user.getEmail(), user.getRole().name(), user.getId());
    }
}
