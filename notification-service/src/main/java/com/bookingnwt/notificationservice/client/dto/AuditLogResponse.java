package com.bookingnwt.notificationservice.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String action;
    private String entityType;
    private Long entityId;
    private String details;
    private String ipAddress;
    private LocalDateTime createdAt;
}
