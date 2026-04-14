package com.bookingnwt.systemevents.controller;

import com.bookingnwt.systemevents.dto.AuditLogRequestDTO;
import com.bookingnwt.systemevents.dto.AuditLogResponseDTO;
import com.bookingnwt.systemevents.service.AuditLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PostMapping
    public ResponseEntity<AuditLogResponseDTO> createAuditLog(@Valid @RequestBody AuditLogRequestDTO dto) {
        return new ResponseEntity<>(auditLogService.createAuditLog(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogResponseDTO> getAuditLogById(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.getAuditLogById(id));
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponseDTO>> getAllAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAllAuditLogs());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogResponseDTO>> getAuditLogsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(auditLogService.getAuditLogsByUserId(userId));
    }

    @GetMapping("/entity-type/{entityType}")
    public ResponseEntity<List<AuditLogResponseDTO>> getAuditLogsByEntityType(@PathVariable String entityType) {
        return ResponseEntity.ok(auditLogService.getAuditLogsByEntityType(entityType));
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLogResponseDTO>> getAuditLogsByAction(@PathVariable String action) {
        return ResponseEntity.ok(auditLogService.getAuditLogsByAction(action));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuditLog(@PathVariable Long id) {
        auditLogService.deleteAuditLog(id);
        return ResponseEntity.noContent().build();
    }
}
