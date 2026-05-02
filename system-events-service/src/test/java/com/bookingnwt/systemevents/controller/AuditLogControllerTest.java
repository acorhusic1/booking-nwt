package com.bookingnwt.systemevents.controller;

import com.bookingnwt.systemevents.dto.AuditLogRequestDTO;
import com.bookingnwt.systemevents.dto.AuditLogResponseDTO;
import com.bookingnwt.systemevents.exception.GlobalExceptionHandler;
import com.bookingnwt.systemevents.exception.ResourceNotFoundException;
import com.bookingnwt.systemevents.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditLogController.class)
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
        objectMapper = JsonMapper.builder().findAndAddModules().build();

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
    void getAuditLogById_Success() throws Exception {
        when(auditLogService.getAuditLogById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/audit-logs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.action").value("CREATE"));
    }

    @Test
    void getAuditLogById_NotFound() throws Exception {
        when(auditLogService.getAuditLogById(99L)).thenThrow(new ResourceNotFoundException("Audit log sa ID 99 nije pronađen"));

        mockMvc.perform(get("/api/audit-logs/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllAuditLogs_Success() throws Exception {
        AuditLogResponseDTO r2 = new AuditLogResponseDTO();
        r2.setId(2L);
        r2.setAction("UPDATE");

        when(auditLogService.getAllAuditLogs()).thenReturn(Arrays.asList(responseDTO, r2));

        mockMvc.perform(get("/api/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAuditLogsByUserId_Success() throws Exception {
        when(auditLogService.getAuditLogsByUserId(2L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/audit-logs/user/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(2));
    }

    @Test
    void getAuditLogsByEntityType_Success() throws Exception {
        when(auditLogService.getAuditLogsByEntityType("PROPERTY")).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/audit-logs/entity-type/PROPERTY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].entityType").value("PROPERTY"));
    }

    @Test
    void getAuditLogsByAction_Success() throws Exception {
        when(auditLogService.getAuditLogsByAction("CREATE")).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/audit-logs/action/CREATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].action").value("CREATE"));
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
