package com.bookingnwt.systemevents.service;

import com.bookingnwt.systemevents.dto.AuditLogBatchRequestDTO;
import com.bookingnwt.systemevents.dto.AuditLogPatchDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
        auditLog.setDetails("Kreiran objekat");
        auditLog.setIpAddress("192.168.1.10");
        auditLog.setCreatedAt(LocalDateTime.now());

        requestDTO = new AuditLogRequestDTO();
        requestDTO.setUserId(2L);
        requestDTO.setAction("CREATE");
        requestDTO.setEntityType("PROPERTY");
        requestDTO.setEntityId(10L);
        requestDTO.setDetails("Kreiran objekat");
        requestDTO.setIpAddress("192.168.1.10");

        responseDTO = new AuditLogResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setUserId(2L);
        responseDTO.setAction("CREATE");
        responseDTO.setEntityType("PROPERTY");
        responseDTO.setEntityId(10L);
        responseDTO.setDetails("Kreiran objekat");
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
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void createBatch_Success() {
        AuditLogRequestDTO r2 = new AuditLogRequestDTO();
        r2.setUserId(3L); r2.setAction("UPDATE"); r2.setEntityType("RESERVATION");
        AuditLog a2 = new AuditLog();
        a2.setId(2L); a2.setUserId(3L); a2.setAction("UPDATE"); a2.setEntityType("RESERVATION");

        AuditLogBatchRequestDTO batch = new AuditLogBatchRequestDTO();
        batch.setLogs(Arrays.asList(requestDTO, r2));

        when(auditLogMapper.toEntity(requestDTO)).thenReturn(auditLog);
        when(auditLogMapper.toEntity(r2)).thenReturn(a2);
        when(auditLogRepository.saveAll(anyList())).thenReturn(Arrays.asList(auditLog, a2));
        when(auditLogMapper.toDTO(auditLog)).thenReturn(responseDTO);
        when(auditLogMapper.toDTO(a2)).thenReturn(new AuditLogResponseDTO());

        List<AuditLogResponseDTO> result = auditLogService.createBatch(batch);

        assertEquals(2, result.size());
        verify(auditLogRepository).saveAll(anyList());
    }

    @Test
    void getAuditLogById_Success() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(auditLog));
        when(auditLogMapper.toDTO(auditLog)).thenReturn(responseDTO);

        AuditLogResponseDTO result = auditLogService.getAuditLogById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void getAuditLogById_NotFound() {
        when(auditLogRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> auditLogService.getAuditLogById(99L));
    }

    @Test
    void getAllAuditLogs_Pageable() {
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog), PageRequest.of(0, 10), 1);
        when(auditLogRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(auditLogMapper.toDTO(auditLog)).thenReturn(responseDTO);

        Page<AuditLogResponseDTO> result = auditLogService.getAllAuditLogs(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void filter_WithParams() {
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog), PageRequest.of(0, 10), 1);
        when(auditLogRepository.filter(any(), any(), any(), any(), any(), any())).thenReturn(page);
        when(auditLogMapper.toDTO(auditLog)).thenReturn(responseDTO);

        Page<AuditLogResponseDTO> result = auditLogService.filter(
                2L, "CREATE", "PROPERTY", null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void topActionsSince_Success() {
        Object[] row = new Object[]{"CREATE", 42L};
        when(auditLogRepository.topActionsSince(any())).thenReturn(List.of(row));

        List<Map<String, Object>> result = auditLogService.topActionsSince(LocalDateTime.now().minusDays(1));

        assertEquals(1, result.size());
        assertEquals("CREATE", result.get(0).get("action"));
        assertEquals(42L, result.get(0).get("count"));
    }

    @Test
    void patchAuditLog_OnlySetsProvidedFields() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(auditLog));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditLogMapper.toDTO(any(AuditLog.class))).thenAnswer(inv -> {
            AuditLog a = inv.getArgument(0);
            AuditLogResponseDTO d = new AuditLogResponseDTO();
            d.setId(a.getId());
            d.setDetails(a.getDetails());
            d.setAction(a.getAction());
            return d;
        });

        AuditLogPatchDTO patch = new AuditLogPatchDTO();
        patch.setDetails("Korigovano");

        AuditLogResponseDTO result = auditLogService.patchAuditLog(1L, patch);

        assertEquals("Korigovano", result.getDetails());
        // Action nije bio u patchu, ostaje nepromjenjen.
        assertEquals("CREATE", result.getAction());
    }

    @Test
    void patchAuditLog_NotFound() {
        when(auditLogRepository.findById(99L)).thenReturn(Optional.empty());
        AuditLogPatchDTO patch = new AuditLogPatchDTO();
        patch.setDetails("X");
        assertThrows(ResourceNotFoundException.class, () -> auditLogService.patchAuditLog(99L, patch));
    }

    @Test
    void deleteAuditLog_Success() {
        when(auditLogRepository.existsById(1L)).thenReturn(true);
        doNothing().when(auditLogRepository).deleteById(1L);
        assertDoesNotThrow(() -> auditLogService.deleteAuditLog(1L));
    }

    @Test
    void deleteAuditLog_NotFound() {
        when(auditLogRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> auditLogService.deleteAuditLog(99L));
    }
}
