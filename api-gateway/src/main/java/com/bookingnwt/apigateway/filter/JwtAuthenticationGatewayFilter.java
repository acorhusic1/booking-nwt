package com.bookingnwt.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Task 7 - Globalni JWT validation filter na api-gateway nivou.
 *
 * Strategija:
 *   - Public putanje (login, registracija, swagger, actuator) prolaze bez tokena.
 *   - Sve ostalo zahtjeva validan Bearer token.
 *   - Gateway samo VALIDIRA token (signature + expiry); autorizaciju (role) rade
 *     downstream servisi kroz @PreAuthorize (decentralized authorization).
 *   - Email i role iz tokena se prosljedjuju downstream servisima kao headeri
 *     X-User-Email i X-User-Roles, sto im stedi parsiranje tokena ako im to ne treba.
 *   - Token se NE skida iz Authorization headera — downstream servisi i dalje
 *     mogu raditi punu autorizaciju kroz svoje JwtAuthenticationFilter.
 */
@Slf4j
@Component
public class JwtAuthenticationGatewayFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Value("${app.jwt.public-key}")
    private String publicKeyBase64;

    @Value("${gateway.security.public-paths:/api/auth/**,/api/users/register,/v3/api-docs/**,/swagger-ui/**,/swagger-ui.html,/actuator/**}")
    private List<String> publicPaths;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Nedostaje Authorization Bearer token");
        }

        String token = authHeader.substring(7);
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String email = claims.getSubject();
            List<?> rolesRaw = claims.get("roles", List.class);
            String roles = rolesRaw == null ? "" :
                    rolesRaw.stream().map(Object::toString).collect(Collectors.joining(","));

            ServerHttpRequest mutated = request.mutate()
                    .header("X-User-Email", email == null ? "" : email)
                    .header("X-User-Roles", roles)
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            return unauthorized(exchange, "Token istekao");
        } catch (Exception ex) {
            log.warn("[Gateway] JWT validation failed: {}", ex.getMessage());
            return unauthorized(exchange, "Nevalidan token");
        }
    }

    private boolean isPublicPath(String path) {
        return publicPaths.stream().anyMatch(p -> PATH_MATCHER.match(p, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String reason) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + reason + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // Ide nakon Spring Cloud Gateway routing-a, ali prije proxy-poziva downstream-u.
        return -1;
    }
}
