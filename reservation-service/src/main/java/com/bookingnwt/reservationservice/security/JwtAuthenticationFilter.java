package com.bookingnwt.reservationservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                Jws<Claims> jws = tokenProvider.parse(token);
                Claims claims = jws.getPayload();

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        tokenProvider.getUsername(claims),
                        null,
                        tokenProvider.getAuthorities(claims));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // uid claim — autentifikovani userId koji controlleri citaju umjesto
                // da vjeruju ID-u iz request body-a (sprjecava spoofing tudjeg guestId-a)
                Object uidRaw = claims.get("uid");
                if (uidRaw != null) {
                    request.setAttribute("authUserId", Long.valueOf(uidRaw.toString()));
                }
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
