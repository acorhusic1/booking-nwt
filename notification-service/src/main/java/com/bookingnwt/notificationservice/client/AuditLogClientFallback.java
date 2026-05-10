package com.bookingnwt.notificationservice.client;

import com.bookingnwt.notificationservice.client.dto.AuditLogRequest;
import com.bookingnwt.notificationservice.client.dto.AuditLogResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Task 5 - Resilience4j fallback za sinhroni audit log poziv.
 *
 * Strategija: ako system-events-service ne odgovara (down/timeout/circuit open),
 * notification-service NE smije pasti — notifikacija se i dalje kreira, samo se
 * audit upis preskace i logira lokalno (kasnije moze ici na DLQ/retry).
 */
@Slf4j
@Component
public class AuditLogClientFallback implements AuditLogClient {

    @Override
    public AuditLogResponse logAudit(AuditLogRequest request) {
        log.warn("[AuditLogFallback] system-events-service nedostupan. " +
                        "Audit upis preskocen za action={}, entityType={}, entityId={}, userId={}",
                request.getAction(), request.getEntityType(),
                request.getEntityId(), request.getUserId());
        return null;
    }
}
