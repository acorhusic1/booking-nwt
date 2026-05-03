package com.bookingnwt.reservationservice.controller;

import com.bookingnwt.reservationservice.dto.ProblemReportRequestDTO;
import com.bookingnwt.reservationservice.dto.ProblemReportResponseDTO;
import com.bookingnwt.reservationservice.exception.GlobalExceptionHandler;
import com.bookingnwt.reservationservice.exception.ResourceNotFoundException;
import com.bookingnwt.reservationservice.model.ProblemReportStatus;
import com.bookingnwt.reservationservice.security.JwtAuthenticationFilter;
import com.bookingnwt.reservationservice.security.JwtTokenProvider;
import com.bookingnwt.reservationservice.service.ProblemReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProblemReportController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ProblemReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProblemReportService reportService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    private ProblemReportResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new ProblemReportResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setReservationId(1L);
        responseDTO.setReporterId(10L);
        responseDTO.setCategory("Čistoća");
        responseDTO.setDescription("Soba nije bila čista");
        responseDTO.setStatus(ProblemReportStatus.REPORTED);
        responseDTO.setReportedAt(LocalDateTime.now());
    }

    @Test
    void createReport_Returns201() throws Exception {
        when(reportService.createReport(any(ProblemReportRequestDTO.class))).thenReturn(responseDTO);

        ProblemReportRequestDTO request = new ProblemReportRequestDTO();
        request.setReservationId(1L);
        request.setReporterId(10L);
        request.setCategory("Čistoća");
        request.setDescription("Soba nije bila čista");

        mockMvc.perform(post("/api/problem-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.category").value("Čistoća"));
    }

    @Test
    void getReport_Returns200() throws Exception {
        when(reportService.getReportById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/problem-reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getReport_NotFound() throws Exception {
        when(reportService.getReportById(99L)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/problem-reports/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllReports_Returns200() throws Exception {
        when(reportService.getAllReports()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/problem-reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByReservation_Returns200() throws Exception {
        when(reportService.getReportsByReservation(1L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/problem-reports/reservation/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByReporter_Returns200() throws Exception {
        when(reportService.getReportsByReporter(10L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/problem-reports/reporter/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateStatus_Returns200() throws Exception {
        responseDTO.setStatus(ProblemReportStatus.RESOLVED);
        when(reportService.updateStatus(eq(1L), eq(ProblemReportStatus.RESOLVED))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/problem-reports/1/status")
                        .param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    void deleteReport_Returns204() throws Exception {
        doNothing().when(reportService).deleteReport(1L);

        mockMvc.perform(delete("/api/problem-reports/1"))
                .andExpect(status().isNoContent());
    }
}
