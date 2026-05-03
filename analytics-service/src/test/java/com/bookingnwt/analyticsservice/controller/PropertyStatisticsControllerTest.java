package com.bookingnwt.analyticsservice.controller;

import com.bookingnwt.analyticsservice.dto.PropertyStatisticsRequestDTO;
import com.bookingnwt.analyticsservice.dto.PropertyStatisticsResponseDTO;
import com.bookingnwt.analyticsservice.exception.GlobalExceptionHandler;
import com.bookingnwt.analyticsservice.exception.ResourceNotFoundException;
import com.bookingnwt.analyticsservice.service.PropertyStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

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

@WebMvcTest(PropertyStatisticsController.class)
@Import(GlobalExceptionHandler.class)
class PropertyStatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PropertyStatisticsService statisticsService;

    private ObjectMapper objectMapper;
    private PropertyStatisticsResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();

        responseDTO = new PropertyStatisticsResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setPropertyId(1L);
        responseDTO.setHostId(2L);
        responseDTO.setYear(2026);
        responseDTO.setMonth(4);
        responseDTO.setTotalReservations(7);
        responseDTO.setTotalRevenue(new BigDecimal("1890.00"));
        responseDTO.setAverageRating(new BigDecimal("4.82"));
        responseDTO.setOccupancyRate(new BigDecimal("62.00"));
        responseDTO.setViewCount(250);
        responseDTO.setCancellationCount(1);
        responseDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createStatistics_Success() throws Exception {
        when(statisticsService.createStatistics(any(PropertyStatisticsRequestDTO.class))).thenReturn(responseDTO);

        PropertyStatisticsRequestDTO request = new PropertyStatisticsRequestDTO();
        request.setPropertyId(1L);
        request.setHostId(2L);
        request.setYear(2026);
        request.setMonth(4);
        request.setTotalReservations(7);
        request.setTotalRevenue(new BigDecimal("1890.00"));
        request.setViewCount(250);
        request.setCancellationCount(1);

        mockMvc.perform(post("/api/statistics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.propertyId").value(1))
                .andExpect(jsonPath("$.year").value(2026));
    }

    @Test
    void createStatistics_ValidationError() throws Exception {
        PropertyStatisticsRequestDTO request = new PropertyStatisticsRequestDTO();

        mockMvc.perform(post("/api/statistics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStatisticsById_Success() throws Exception {
        when(statisticsService.getStatisticsById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/statistics/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getStatisticsById_NotFound() throws Exception {
        when(statisticsService.getStatisticsById(99L)).thenThrow(new ResourceNotFoundException("Statistika sa ID 99 nije pronađena"));

        mockMvc.perform(get("/api/statistics/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllStatistics_Success() throws Exception {
        PropertyStatisticsResponseDTO r2 = new PropertyStatisticsResponseDTO();
        r2.setId(2L);

        when(statisticsService.getAllStatistics()).thenReturn(Arrays.asList(responseDTO, r2));

        mockMvc.perform(get("/api/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getStatisticsByPropertyId_Success() throws Exception {
        when(statisticsService.getStatisticsByPropertyId(1L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/statistics/property/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getStatisticsByHostId_Success() throws Exception {
        when(statisticsService.getStatisticsByHostId(2L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/statistics/host/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getStatisticsByHostIdAndPeriod_Success() throws Exception {
        when(statisticsService.getStatisticsByHostIdAndPeriod(2L, 2026, 4)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/statistics/host/2/period?year=2026&month=4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deleteStatistics_Success() throws Exception {
        doNothing().when(statisticsService).deleteStatistics(1L);

        mockMvc.perform(delete("/api/statistics/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteStatistics_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Statistika sa ID 99 nije pronađena"))
                .when(statisticsService).deleteStatistics(99L);

        mockMvc.perform(delete("/api/statistics/99"))
                .andExpect(status().isNotFound());
    }
}
