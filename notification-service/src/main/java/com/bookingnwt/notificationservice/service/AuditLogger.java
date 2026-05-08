package com.bookingnwt.notificationservice.service;

import com.bookingnwt.notificationservice.client.AuditLogClient;
import com.bookingnwt.notificationservice.client.dto.AuditLogRequest;
import com.bookingnwt.notificationservice.client.dto.AuditLogResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Task 5 - Wrapper oko Feign klijenta. Centralizuje audit log pozive kako poslovni
 * kod ne bi morao znati o tipovima downstream servisa.
 *
 * Fallback klasa AuditLogClientFallback hvata sve greske, vraca null i logira
 * lokalno. Tako poslovne operacije (kreiranje notifikacije) ne padaju kada je
 * audit servis privremeno nedostupan.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogger {

    private final AuditLogClient auditLogClient;

    public AuditLogResponse log(Long userId, String action, String entityType,
                                 Long entityId, String details) {
        AuditLogRequest request = AuditLogRequest.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress("internal:notification-service")
                .build();
        try {
            return auditLogClient.logAudit(request);
        } catch (Exception ex) {
            log.warn("[AuditLogger] Audit log poziv pao: {}", ex.getMessage());
            return null;
        }
    }
}
