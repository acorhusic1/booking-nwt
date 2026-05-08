package com.bookingnwt.notificationservice.client;

import com.bookingnwt.notificationservice.client.dto.AuditLogRequest;
import com.bookingnwt.notificationservice.client.dto.AuditLogResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Task 5 - Sinhroni Feign poziv prema system-events-service za upis audit log unosa.
 *
 * Eureka resolva 'system-events-service' u IP:port (nema hardkodiranih adresa).
 * Resilience4j circuit breaker je konfigurisan kroz spring.cloud.openfeign.circuitbreaker.enabled
 * + Resilience4j instance "system-events-service" (vidi config-server properties).
 */
@FeignClient(name = "system-events-service", fallback = AuditLogClientFallback.class)
public interface AuditLogClient {

    @PostMapping("/api/audit-logs")
    AuditLogResponse logAudit(@RequestBody AuditLogRequest request);
}
