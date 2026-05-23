package com.bookingnwt.paymentservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final PublicKey verifyingKey;

    public JwtTokenProvider(@Value("${app.jwt.public-key}") String publicKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            this.verifyingKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (Exception ex) {
            throw new RuntimeException("Greška pri učitavanju RSA javnog ključa", ex);
        }
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(verifyingKey)
                .build()
                .parseSignedClaims(token);
    }

    public String getUsername(Claims claims) {
        return claims.getSubject();
    }

    public Collection<GrantedAuthority> getAuthorities(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof List<?> list) {
            return list.stream()
                    .map(Object::toString)
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .map(GrantedAuthority.class::cast)
                    .toList();
        }
        return Collections.emptyList();
    }
}
