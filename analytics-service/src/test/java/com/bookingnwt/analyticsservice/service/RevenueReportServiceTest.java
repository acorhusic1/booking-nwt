package com.bookingnwt.analyticsservice.service;

import com.bookingnwt.analyticsservice.dto.RevenueReportRequestDTO;
import com.bookingnwt.analyticsservice.dto.RevenueReportResponseDTO;
import com.bookingnwt.analyticsservice.exception.ResourceNotFoundException;
import com.bookingnwt.analyticsservice.mapper.RevenueReportMapper;
import com.bookingnwt.analyticsservice.model.RevenueReport;
import com.bookingnwt.analyticsservice.repository.RevenueReportRepository;
import com.bookingnwt.analyticsservice.service.impl.RevenueReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevenueReportServiceTest {

    @Mock
    private RevenueReportRepository repository;

    @Mock
    private RevenueReportMapper mapper;

    @InjectMocks
    private RevenueReportServiceImpl service;

    private RevenueReport entity;
    private RevenueReportRequestDTO requestDTO;
    private RevenueReportResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        entity = new RevenueReport();
        entity.setId(1L);
        entity.setHostId(2L);
        entity.setYear(2026);
        entity.setMonth(4);
        entity.setTotalRevenue(new BigDecimal("3090.00"));
        entity.setPlatformCommission(new BigDecimal("463.50"));
        entity.setNetRevenue(new BigDecimal("2626.50"));
        entity.setTotalReservations(11);
        entity.setTotalCancellations(1);
        entity.setTotalProperties(2);
        entity.setAverageOccupancyRate(new BigDecimal("56.00"));
        entity.setCreatedAt(LocalDateTime.now());

        requestDTO = new RevenueReportRequestDTO();
        requestDTO.setHostId(2L);
        requestDTO.setYear(2026);
        requestDTO.setMonth(4);
        requestDTO.setTotalRevenue(new BigDecimal("3090.00"));
        requestDTO.setPlatformCommission(new BigDecimal("463.50"));
        requestDTO.setNetRevenue(new BigDecimal("2626.50"));
        requestDTO.setTotalReservations(11);
        requestDTO.setTotalCancellations(1);
        requestDTO.setTotalProperties(2);
        requestDTO.setAverageOccupancyRate(new BigDecimal("56.00"));

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
        responseDTO.setCreatedAt(entity.getCreatedAt());
    }

    @Test
    void createReport_Success() {
        when(mapper.toEntity(requestDTO)).thenReturn(entity);
        when(repository.save(any(RevenueReport.class))).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(responseDTO);

        RevenueReportResponseDTO result = service.createReport(requestDTO);

        assertNotNull(result);
        assertEquals(2026, result.getYear());
        assertEquals(new BigDecimal("3090.00"), result.getTotalRevenue());
        verify(repository, times(1)).save(any(RevenueReport.class));
    }

    @Test
    void getReportById_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(responseDTO);

        RevenueReportResponseDTO result = service.getReportById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getReportById_NotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getReportById(99L));
    }

    @Test
    void getAllReports_Success() {
        RevenueReport e2 = new RevenueReport();
        e2.setId(2L);
        RevenueReportResponseDTO r2 = new RevenueReportResponseDTO();
        r2.setId(2L);

        when(repository.findAll()).thenReturn(Arrays.asList(entity, e2));
        when(mapper.toDTO(entity)).thenReturn(responseDTO);
        when(mapper.toDTO(e2)).thenReturn(r2);

        List<RevenueReportResponseDTO> result = service.getAllReports();

        assertEquals(2, result.size());
    }

    @Test
    void getReportsByHostId_Success() {
        when(repository.findByHostId(2L)).thenReturn(List.of(entity));
        when(mapper.toDTO(entity)).thenReturn(responseDTO);

        List<RevenueReportResponseDTO> result = service.getReportsByHostId(2L);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getHostId());
    }

    @Test
    void getReportsByHostIdAndYear_Success() {
        when(repository.findByHostIdAndYear(2L, 2026)).thenReturn(List.of(entity));
        when(mapper.toDTO(entity)).thenReturn(responseDTO);

        List<RevenueReportResponseDTO> result = service.getReportsByHostIdAndYear(2L, 2026);

        assertEquals(1, result.size());
    }

    @Test
    void getReportsByPeriod_Success() {
        when(repository.findByYearAndMonth(2026, 4)).thenReturn(List.of(entity));
        when(mapper.toDTO(entity)).thenReturn(responseDTO);

        List<RevenueReportResponseDTO> result = service.getReportsByPeriod(2026, 4);

        assertEquals(1, result.size());
    }

    @Test
    void deleteReport_Success() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        assertDoesNotThrow(() -> service.deleteReport(1L));
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void deleteReport_NotFound() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteReport(99L));
    }
}
