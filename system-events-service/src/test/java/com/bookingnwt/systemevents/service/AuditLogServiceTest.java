package com.bookingnwt.systemevents.service;

import com.bookingnwt.systemevents.dto.AuditLogRequestDTO;
import com.bookingnwt.systemevents.dto.AuditLogResponseDTO;
import com.bookingnwt.systemevents.exception.ResourceNotFoundException;
import com.bookingnwt.systemevents.mapper.AuditLogMapper;
import com.bookingnwt.systemevents.model.AuditLog;
import com.bookingnwt.systemevents.repository.AuditLogRepository;
import com.bookingnwt.systemevents.service.impl.AuditLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private AuditLog auditLog;
    private AuditLogRequestDTO requestDTO;
    private AuditLogResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
        auditLog.setId(1L);
        auditLog.setUserId(2L);
        auditLog.setAction("CREATE");
        auditLog.setEntityType("PROPERTY");
        auditLog.setEntityId(10L);
        auditLog.setDetails("Kreiran objekat: Apartman Baščaršija");
        auditLog.setIpAddress("192.168.1.10");
        auditLog.setCreatedAt(LocalDateTime.now());

        requestDTO = new AuditLogRequestDTO();
        requestDTO.setUserId(2L);
        requestDTO.setAction("CREATE");
        requestDTO.setEntityType("PROPERTY");
        requestDTO.setEntityId(10L);
        requestDTO.setDetails("Kreiran objekat: Apartman Baščaršija");
        requestDTO.setIpAddress("192.168.1.10");

        responseDTO = new AuditLogResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setUserId(2L);
        responseDTO.setAction("CREATE");
        responseDTO.setEntityType("PROPERTY");
        responseDTO.setEntityId(10L);
        responseDTO.setDetails("Kreiran objekat: Apartman Baščaršija");
        responseDTO.setIpAddress("192.168.1.10");
        responseDTO.setCreatedAt(auditLog.getCreatedAt());
    }

    @Test
    void createAuditLog_Success() {
        when(auditLogMapper.toEntity(requestDTO)).thenReturn(auditLog);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);
        when(auditLogMapper.toDTO(auditLog)).thenReturn(responseDTO);

        AuditLogResponseDTO result = auditLogService.createAuditLog(requestDTO);

        assertNotNull(result);
        assertEquals("CREATE", result.getAction());
        assertEquals("PROPERTY", result.getEntityType());
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void getAuditLogById_Success() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(auditLog));
        when(auditLogMapper.toDTO(auditLog)).thenReturn(responseDTO);

        AuditLogResponseDTO result = auditLogService.getAuditLogById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getAuditLogById_NotFound() {
        when(auditLogRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> auditLogService.getAuditLogById(99L));
    }

    @Test
    void getAllAuditLogs_Success() {
        AuditLog a2 = new AuditLog();
        a2.setId(2L);
        AuditLogResponseDTO r2 = new AuditLogResponseDTO();
        r2.setId(2L);

        when(auditLogRepository.findAll()).thenReturn(Arrays.asList(auditLog, a2));
        when(auditLogMapper.toDTO(auditLog)).thenReturn(responseDTO);
        when(auditLogMapper.toDTO(a2)).thenReturn(r2);

        List<AuditLogResponseDTO> result = auditLogService.getAllAuditLogs();

        assertEquals(2, result.size());
    }

    @Test
    void getAuditLogsByUserId_Success() {
        when(auditLogRepository.findByUserIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(auditLog));
        when(auditLogMapper.toDTO(auditLog)).thenReturn(responseDTO);

        List<AuditLogResponseDTO> result = auditLogService.getAuditLogsByUserId(2L);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getUserId());
    }

    @Test
    void getAuditLogsByEntityType_Success() {
        when(auditLogRepository.findByEntityTypeOrderByCreatedAtDesc("PROPERTY")).thenReturn(List.of(auditLog));
        when(auditLogMapper.toDTO(auditLog)).thenReturn(responseDTO);

        List<AuditLogResponseDTO> result = auditLogService.getAuditLogsByEntityType("PROPERTY");

        assertEquals(1, result.size());
        assertEquals("PROPERTY", result.get(0).getEntityType());
    }

    @Test
    void getAuditLogsByAction_Success() {
        when(auditLogRepository.findByActionOrderByCreatedAtDesc("CREATE")).thenReturn(List.of(auditLog));
        when(auditLogMapper.toDTO(auditLog)).thenReturn(responseDTO);

        List<AuditLogResponseDTO> result = auditLogService.getAuditLogsByAction("CREATE");

        assertEquals(1, result.size());
        assertEquals("CREATE", result.get(0).getAction());
    }

    @Test
    void deleteAuditLog_Success() {
        when(auditLogRepository.existsById(1L)).thenReturn(true);
        doNothing().when(auditLogRepository).deleteById(1L);

        assertDoesNotThrow(() -> auditLogService.deleteAuditLog(1L));
        verify(auditLogRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteAuditLog_NotFound() {
        when(auditLogRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> auditLogService.deleteAuditLog(99L));
    }
}
