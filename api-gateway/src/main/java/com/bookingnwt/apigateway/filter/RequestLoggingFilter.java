package com.bookingnwt.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * Task 7 - Cross-cutting concern: request logging na gateway nivou.
 * Logira metoda, putanju, status i trajanje za svaki request koji prodje kroz gateway.
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Instant start = Instant.now();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        String remote = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getHostString() : "unknown";

        return chain.filter(exchange).doFinally(signal -> {
            long ms = Duration.between(start, Instant.now()).toMillis();
            int status = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value() : 0;
            log.info("[Gateway] {} {} → {} ({} ms) from {}", method, path, status, ms, remote);
        });
    }

    @Override
    public int getOrder() {
        // Pred-request log: ide prije svih filtera (najraniji).
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
