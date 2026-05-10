package com.bookingnwt.systemevents.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Task 4 - PATCH (parcijalno azuriranje audit log unosa).
 *
 * Tipicno se koristi samo za korekciju "details" polja od strane admina kada
 * je originalni unos nepotpun. Sva polja su opcionalna.
 */
@Getter
@Setter
@NoArgsConstructor
public class AuditLogPatchDTO {

    private String action;

    private String entityType;

    private Long entityId;

    private String details;

    private String ipAddress;
}
