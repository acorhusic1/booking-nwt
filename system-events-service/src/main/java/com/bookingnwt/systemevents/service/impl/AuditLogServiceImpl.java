package com.bookingnwt.systemevents.service.impl;

import com.bookingnwt.systemevents.dto.AuditLogBatchRequestDTO;
import com.bookingnwt.systemevents.dto.AuditLogPatchDTO;
import com.bookingnwt.systemevents.dto.AuditLogRequestDTO;
import com.bookingnwt.systemevents.dto.AuditLogResponseDTO;
import com.bookingnwt.systemevents.exception.ResourceNotFoundException;
import com.bookingnwt.systemevents.mapper.AuditLogMapper;
import com.bookingnwt.systemevents.model.AuditLog;
import com.bookingnwt.systemevents.repository.AuditLogRepository;
import com.bookingnwt.systemevents.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional
    public AuditLogResponseDTO createAuditLog(AuditLogRequestDTO dto) {
        AuditLog auditLog = auditLogMapper.toEntity(dto);
        return auditLogMapper.toDTO(auditLogRepository.save(auditLog));
    }

    @Override
    @Transactional
    public List<AuditLogResponseDTO> createBatch(AuditLogBatchRequestDTO batch) {
        // Task 4 - Batch insert preko saveAll() (Hibernate jdbc.batch_size aktivan).
        List<AuditLog> entities = batch.getLogs().stream()
                .map(auditLogMapper::toEntity)
                .toList();
        return auditLogRepository.saveAll(entities).stream()
                .map(auditLogMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponseDTO getAuditLogById(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log sa ID " + id + " nije pronađen"));
        return auditLogMapper.toDTO(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDTO> getAllAuditLogs() {
        return auditLogRepository.findAll().stream()
                .map(auditLogMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(auditLogMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDTO> getAuditLogsByUserId(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(auditLogMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> getAuditLogsByUserId(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable).map(auditLogMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDTO> getAuditLogsByEntityType(String entityType) {
        return auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType).stream()
                .map(auditLogMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDTO> getAuditLogsByAction(String action) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action).stream()
                .map(auditLogMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> filter(Long userId, String action, String entityType,
                                            LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return auditLogRepository.filter(userId, action, entityType, from, to, pageable)
                .map(auditLogMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> topActionsSince(LocalDateTime from) {
        return auditLogRepository.topActionsSince(from).stream()
                .map(row -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("action", row[0]);
                    entry.put("count", ((Number) row[1]).longValue());
                    return entry;
                })
                .toList();
    }

    @Override
    @Transactional
    public AuditLogResponseDTO patchAuditLog(Long id, AuditLogPatchDTO patch) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log sa ID " + id + " nije pronađen"));

        if (patch.getAction() != null)     auditLog.setAction(patch.getAction());
        if (patch.getEntityType() != null) auditLog.setEntityType(patch.getEntityType());
        if (patch.getEntityId() != null)   auditLog.setEntityId(patch.getEntityId());
        if (patch.getDetails() != null)    auditLog.setDetails(patch.getDetails());
        if (patch.getIpAddress() != null)  auditLog.setIpAddress(patch.getIpAddress());

        return auditLogMapper.toDTO(auditLogRepository.save(auditLog));
    }

    @Override
    @Transactional
    public void deleteAuditLog(Long id) {
        if (!auditLogRepository.existsById(id)) {
            throw new ResourceNotFoundException("Audit log sa ID " + id + " nije pronađen");
        }
        auditLogRepository.deleteById(id);
    }
}
