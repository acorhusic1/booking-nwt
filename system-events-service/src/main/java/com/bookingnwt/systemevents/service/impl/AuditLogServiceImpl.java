package com.bookingnwt.systemevents.service.impl;

import com.bookingnwt.systemevents.dto.AuditLogRequestDTO;
import com.bookingnwt.systemevents.dto.AuditLogResponseDTO;
import com.bookingnwt.systemevents.exception.ResourceNotFoundException;
import com.bookingnwt.systemevents.mapper.AuditLogMapper;
import com.bookingnwt.systemevents.model.AuditLog;
import com.bookingnwt.systemevents.repository.AuditLogRepository;
import com.bookingnwt.systemevents.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    public AuditLogResponseDTO createAuditLog(AuditLogRequestDTO dto) {
        AuditLog auditLog = auditLogMapper.toEntity(dto);
        return auditLogMapper.toDTO(auditLogRepository.save(auditLog));
    }

    @Override
    public AuditLogResponseDTO getAuditLogById(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log sa ID " + id + " nije pronađen"));
        return auditLogMapper.toDTO(auditLog);
    }

    @Override
    public List<AuditLogResponseDTO> getAllAuditLogs() {
        return auditLogRepository.findAll().stream()
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLogResponseDTO> getAuditLogsByUserId(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLogResponseDTO> getAuditLogsByEntityType(String entityType) {
        return auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType).stream()
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLogResponseDTO> getAuditLogsByAction(String action) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action).stream()
                .map(auditLogMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAuditLog(Long id) {
        if (!auditLogRepository.existsById(id)) {
            throw new ResourceNotFoundException("Audit log sa ID " + id + " nije pronađen");
        }
        auditLogRepository.deleteById(id);
    }
}
