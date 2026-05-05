package com.bookingnwt.systemevents.controller;

import com.bookingnwt.systemevents.dto.AuditLogBatchRequestDTO;
import com.bookingnwt.systemevents.dto.AuditLogPatchDTO;
import com.bookingnwt.systemevents.dto.AuditLogRequestDTO;
import com.bookingnwt.systemevents.dto.AuditLogResponseDTO;
import com.bookingnwt.systemevents.exception.GlobalExceptionHandler;
import com.bookingnwt.systemevents.exception.ResourceNotFoundException;
import com.bookingnwt.systemevents.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuditLogController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
}, excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {
                com.bookingnwt.systemevents.security.SecurityConfig.class,
                com.bookingnwt.systemevents.security.JwtAuthenticationFilter.class
        }))
@Import(GlobalExceptionHandler.class)
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogService auditLogService;

    private ObjectMapper objectMapper;
    private AuditLogResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        responseDTO = new AuditLogResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setUserId(2L);
        responseDTO.setAction("CREATE");
        responseDTO.setEntityType("PROPERTY");
        responseDTO.setEntityId(10L);
        responseDTO.setDetails("Kreiran objekat: Apartman Baščaršija");
        responseDTO.setIpAddress("192.168.1.10");
        responseDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createAuditLog_Success() throws Exception {
        when(auditLogService.createAuditLog(any(AuditLogRequestDTO.class))).thenReturn(responseDTO);

        AuditLogRequestDTO request = new AuditLogRequestDTO();
        request.setUserId(2L);
        request.setAction("CREATE");
        request.setEntityType("PROPERTY");
        request.setEntityId(10L);
        request.setDetails("Kreiran objekat");
        request.setIpAddress("192.168.1.10");

        mockMvc.perform(post("/api/audit-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.action").value("CREATE"))
                .andExpect(jsonPath("$.entityType").value("PROPERTY"));
    }

    @Test
    void createAuditLog_ValidationError() throws Exception {
        AuditLogRequestDTO request = new AuditLogRequestDTO();

        mockMvc.perform(post("/api/audit-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBatch_Success() throws Exception {
        AuditLogRequestDTO r1 = new AuditLogRequestDTO();
        r1.setUserId(1L); r1.setAction("CREATE"); r1.setEntityType("X");
        AuditLogRequestDTO r2 = new AuditLogRequestDTO();
        r2.setUserId(2L); r2.setAction("UPDATE"); r2.setEntityType("Y");

        AuditLogBatchRequestDTO batch = new AuditLogBatchRequestDTO();
        batch.setLogs(Arrays.asList(r1, r2));

        when(auditLogService.createBatch(any())).thenReturn(List.of(responseDTO, responseDTO));

        mockMvc.perform(post("/api/audit-logs/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void createBatch_EmptyList_ValidationError() throws Exception {
        AuditLogBatchRequestDTO batch = new AuditLogBatchRequestDTO();
        batch.setLogs(List.of());

        mockMvc.perform(post("/api/audit-logs/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAuditLogById_Success() throws Exception {
        when(auditLogService.getAuditLogById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/audit-logs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAuditLogById_NotFound() throws Exception {
        when(auditLogService.getAuditLogById(99L))
                .thenThrow(new ResourceNotFoundException("Audit log sa ID 99 nije pronađen"));

        mockMvc.perform(get("/api/audit-logs/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllAuditLogs_Pageable() throws Exception {
        Page<AuditLogResponseDTO> page = new PageImpl<>(List.of(responseDTO), PageRequest.of(0, 50), 1);
        when(auditLogService.getAllAuditLogs(any())).thenReturn(page);

        mockMvc.perform(get("/api/audit-logs?page=0&size=50&sort=createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void filter_WithParams_Success() throws Exception {
        Page<AuditLogResponseDTO> page = new PageImpl<>(List.of(responseDTO), PageRequest.of(0, 10), 1);
        when(auditLogService.filter(any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/audit-logs/filter?action=CREATE&entityType=PROPERTY&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void topActionsSince_Success() throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("action", "CREATE");
        row.put("count", 42L);
        when(auditLogService.topActionsSince(any())).thenReturn(List.of(row));

        mockMvc.perform(get("/api/audit-logs/stats/top-actions?from=2026-01-01T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].action").value("CREATE"))
                .andExpect(jsonPath("$[0].count").value(42));
    }

    @Test
    void patchAuditLog_Success() throws Exception {
        AuditLogPatchDTO patch = new AuditLogPatchDTO();
        patch.setDetails("Korigovani detalji");

        AuditLogResponseDTO patched = new AuditLogResponseDTO();
        patched.setId(1L);
        patched.setDetails("Korigovani detalji");
        when(auditLogService.patchAuditLog(any(Long.class), any(AuditLogPatchDTO.class))).thenReturn(patched);

        mockMvc.perform(patch("/api/audit-logs/1")
                        .contentType("application/merge-patch+json")
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details").value("Korigovani detalji"));
    }

    @Test
    void patchAuditLog_NotFound() throws Exception {
        AuditLogPatchDTO patch = new AuditLogPatchDTO();
        patch.setDetails("X");
        when(auditLogService.patchAuditLog(anyLong(), any(AuditLogPatchDTO.class)))
                .thenThrow(new ResourceNotFoundException("Audit log sa ID 99 nije pronađen"));

        mockMvc.perform(patch("/api/audit-logs/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAuditLogsByEntityType_Success() throws Exception {
        when(auditLogService.getAuditLogsByEntityType("PROPERTY")).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/audit-logs/entity-type/PROPERTY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAuditLogsByAction_Success() throws Exception {
        when(auditLogService.getAuditLogsByAction("CREATE")).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/audit-logs/action/CREATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deleteAuditLog_Success() throws Exception {
        doNothing().when(auditLogService).deleteAuditLog(1L);
        mockMvc.perform(delete("/api/audit-logs/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAuditLog_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Audit log sa ID 99 nije pronađen"))
                .when(auditLogService).deleteAuditLog(99L);

        mockMvc.perform(delete("/api/audit-logs/99"))
                .andExpect(status().isNotFound());
    }
}
