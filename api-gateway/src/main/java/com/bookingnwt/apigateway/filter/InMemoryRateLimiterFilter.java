package com.bookingnwt.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Task 7 - Cross-cutting concern: in-memory token bucket rate limiter po IP-u.
 *
 * Lagana implementacija (bez Redis-a) prikladna za potrebe vjezbe i development.
 * Za produkciju bi se koristio RedisRateLimiter koji dolazi sa Spring Cloud Gateway.
 */
@Slf4j
@Component
public class InMemoryRateLimiterFilter implements GlobalFilter, Ordered {

    @Value("${gateway.rate-limit.replenish-rate:20}")
    private int replenishRatePerSecond;

    @Value("${gateway.rate-limit.burst-capacity:40}")
    private int burstCapacity;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String key = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getHostString()
                : "unknown";
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(burstCapacity));

        if (!bucket.tryConsume(replenishRatePerSecond, burstCapacity)) {
            log.warn("[RateLimiter] {} blokiran (prekoracen limit)", key);
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            String body = "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Prekoracen rate limit\"}";
            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Prije JWT filtera, da DDoS pokusaji ne troše CPU na parsiranje tokena.
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    private static final class Bucket {
        private final AtomicLong tokens;
        private volatile long lastRefillNanos;

        Bucket(int initial) {
            this.tokens = new AtomicLong(initial);
            this.lastRefillNanos = System.nanoTime();
        }

        synchronized boolean tryConsume(int refillPerSecond, int capacity) {
            long now = System.nanoTime();
            long elapsedNanos = now - lastRefillNanos;
            long refill = (elapsedNanos * refillPerSecond) / 1_000_000_000L;
            if (refill > 0) {
                long updated = Math.min(capacity, tokens.get() + refill);
                tokens.set(updated);
                lastRefillNanos = now;
            }
            if (tokens.get() <= 0) return false;
            tokens.decrementAndGet();
            return true;
        }
    }
}
