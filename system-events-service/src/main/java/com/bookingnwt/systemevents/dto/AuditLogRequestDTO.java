package com.bookingnwt.systemevents.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuditLogRequestDTO {

    @NotNull(message = "ID korisnika je obavezan")
    private Long userId;

    @NotBlank(message = "Akcija je obavezna")
    private String action;

    @NotBlank(message = "Tip entiteta je obavezan")
    private String entityType;

    private Long entityId;

    private String details;

    private String ipAddress;
}
