package com.bookingnwt.propertyservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS se postavlja na api-gateway nivou (jedino mjesto koje browser vidi).
            // Duplikat ovdje stvara duple Access-Control-Allow-Origin headere.
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public GET endpoints — javno pretrazivanje smjestaja
                .requestMatchers(HttpMethod.GET, "/api/properties", "/api/properties/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reviews/property/**").permitAll()
                // F11 — broj pregleda: i neulogovani posjetioci se broje
                .requestMatchers(HttpMethod.POST, "/api/properties/*/view").permitAll()
                .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                // POST/PUT/PATCH/DELETE i sve ostalo zahtjeva JWT (rola se provjerava u @PreAuthorize)
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
