package com.bookingnwt.systemevents.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Task 4 - Batch unos audit log unosa u jednom HTTP pozivu.
 */
@Getter
@Setter
@NoArgsConstructor
public class AuditLogBatchRequestDTO {

    @NotEmpty(message = "Batch lista ne moze biti prazna")
    @Size(max = 500, message = "Maksimalno 500 audit log unosa po batchu")
    @Valid
    private List<AuditLogRequestDTO> logs;
}
