package com.bookingnwt.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtProvider {

    @Value("${app.jwt.private-key}")
    private String privateKeyBase64;

    @Value("${app.jwt.public-key}")
    private String publicKeyBase64;

    @Value("${app.jwt.expiration:3600000}")
    private long jwtExpirationInMs;

    private PrivateKey getPrivateKey() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    private PublicKey getPublicKey() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    public String generateToken(Authentication authentication) {
        String email = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return generateToken(email, roles);
    }

    public String generateToken(String email, List<String> roles) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

            return Jwts.builder()
                    .subject(email)
                    .claim("roles", roles)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(getPrivateKey())
                    .compact();
        } catch (Exception ex) {
            throw new RuntimeException("Greška pri generiranju JWT tokena", ex);
        }
    }

    public String getEmailFromJwt(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getPublicKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (Exception ex) {
            throw new RuntimeException("Greška pri čitanju JWT tokena", ex);
        }
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getPublicKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception ex) {
            throw new RuntimeException("Greška pri čitanju JWT claims", ex);
        }
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getPublicKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (Exception ex) {
            // Token nevalidan
        }
        return false;
    }
}
