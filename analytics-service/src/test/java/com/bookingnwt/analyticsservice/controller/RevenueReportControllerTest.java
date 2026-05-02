package com.bookingnwt.analyticsservice.controller;

import com.bookingnwt.analyticsservice.dto.RevenueReportRequestDTO;
import com.bookingnwt.analyticsservice.dto.RevenueReportResponseDTO;
import com.bookingnwt.analyticsservice.exception.GlobalExceptionHandler;
import com.bookingnwt.analyticsservice.exception.ResourceNotFoundException;
import com.bookingnwt.analyticsservice.service.RevenueReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RevenueReportController.class)
@Import(GlobalExceptionHandler.class)
class RevenueReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RevenueReportService reportService;

    private ObjectMapper objectMapper;
    private RevenueReportResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();

        responseDTO = new RevenueReportResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setHostId(2L);
        responseDTO.setYear(2026);
        responseDTO.setMonth(4);
        responseDTO.setTotalRevenue(new BigDecimal("3090.00"));
        responseDTO.setPlatformCommission(new BigDecimal("463.50"));
        responseDTO.setNetRevenue(new BigDecimal("2626.50"));
        responseDTO.setTotalReservations(11);
        responseDTO.setTotalCancellations(1);
        responseDTO.setTotalProperties(2);
        responseDTO.setAverageOccupancyRate(new BigDecimal("56.00"));
        responseDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createReport_Success() throws Exception {
        when(reportService.createReport(any(RevenueReportRequestDTO.class))).thenReturn(responseDTO);

        RevenueReportRequestDTO request = new RevenueReportRequestDTO();
        request.setHostId(2L);
        request.setYear(2026);
        request.setMonth(4);
        request.setTotalRevenue(new BigDecimal("3090.00"));
        request.setPlatformCommission(new BigDecimal("463.50"));
        request.setNetRevenue(new BigDecimal("2626.50"));
        request.setTotalReservations(11);
        request.setTotalCancellations(1);
        request.setTotalProperties(2);

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hostId").value(2))
                .andExpect(jsonPath("$.year").value(2026));
    }

    @Test
    void createReport_ValidationError() throws Exception {
        RevenueReportRequestDTO request = new RevenueReportRequestDTO();

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReportById_Success() throws Exception {
        when(reportService.getReportById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getReportById_NotFound() throws Exception {
        when(reportService.getReportById(99L)).thenThrow(new ResourceNotFoundException("Izvještaj sa ID 99 nije pronađen"));

        mockMvc.perform(get("/api/reports/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllReports_Success() throws Exception {
        RevenueReportResponseDTO r2 = new RevenueReportResponseDTO();
        r2.setId(2L);

        when(reportService.getAllReports()).thenReturn(Arrays.asList(responseDTO, r2));

        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getReportsByHostId_Success() throws Exception {
        when(reportService.getReportsByHostId(2L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/reports/host/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getReportsByHostIdAndYear_Success() throws Exception {
        when(reportService.getReportsByHostIdAndYear(2L, 2026)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/reports/host/2/year/2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getReportsByPeriod_Success() throws Exception {
        when(reportService.getReportsByPeriod(2026, 4)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/reports/period?year=2026&month=4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deleteReport_Success() throws Exception {
        doNothing().when(reportService).deleteReport(1L);

        mockMvc.perform(delete("/api/reports/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReport_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Izvještaj sa ID 99 nije pronađen"))
                .when(reportService).deleteReport(99L);

        mockMvc.perform(delete("/api/reports/99"))
                .andExpect(status().isNotFound());
    }
}
