package com.bookingnwt.systemevents.service;

import com.bookingnwt.systemevents.dto.AuditLogBatchRequestDTO;
import com.bookingnwt.systemevents.dto.AuditLogPatchDTO;
import com.bookingnwt.systemevents.dto.AuditLogRequestDTO;
import com.bookingnwt.systemevents.dto.AuditLogResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditLogService {

    AuditLogResponseDTO createAuditLog(AuditLogRequestDTO dto);

    /**
     * Task 4 - Batch insert (saveAll + Hibernate JDBC batch).
     */
    List<AuditLogResponseDTO> createBatch(AuditLogBatchRequestDTO batch);

    AuditLogResponseDTO getAuditLogById(Long id);

    List<AuditLogResponseDTO> getAllAuditLogs();

    /**
     * Task 4 - Pagination & Sorting na svim audit logovima.
     */
    Page<AuditLogResponseDTO> getAllAuditLogs(Pageable pageable);

    List<AuditLogResponseDTO> getAuditLogsByUserId(Long userId);

    Page<AuditLogResponseDTO> getAuditLogsByUserId(Long userId, Pageable pageable);

    List<AuditLogResponseDTO> getAuditLogsByEntityType(String entityType);

    List<AuditLogResponseDTO> getAuditLogsByAction(String action);

    /**
     * Task 4 - Custom JPQL query (filtriranje + paginacija).
     */
    Page<AuditLogResponseDTO> filter(Long userId, String action, String entityType,
                                     LocalDateTime from, LocalDateTime to, Pageable pageable);

    /**
     * Task 4 - Native SQL query (top N akcija od datuma).
     */
    List<Map<String, Object>> topActionsSince(LocalDateTime from);

    /**
     * Task 4 - PATCH (parcijalno azuriranje).
     */
    AuditLogResponseDTO patchAuditLog(Long id, AuditLogPatchDTO patch);

    void deleteAuditLog(Long id);
}
