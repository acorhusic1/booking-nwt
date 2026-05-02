package com.bookingnwt.systemevents.service;

import com.bookingnwt.systemevents.dto.AuditLogRequestDTO;
import com.bookingnwt.systemevents.dto.AuditLogResponseDTO;

import java.util.List;

public interface AuditLogService {

    AuditLogResponseDTO createAuditLog(AuditLogRequestDTO dto);

    AuditLogResponseDTO getAuditLogById(Long id);

    List<AuditLogResponseDTO> getAllAuditLogs();

    List<AuditLogResponseDTO> getAuditLogsByUserId(Long userId);

    List<AuditLogResponseDTO> getAuditLogsByEntityType(String entityType);

    List<AuditLogResponseDTO> getAuditLogsByAction(String action);

    void deleteAuditLog(Long id);
}
