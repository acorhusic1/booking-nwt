package com.bookingnwt.analyticsservice.service;

import com.bookingnwt.analyticsservice.dto.PropertyStatisticsRequestDTO;
import com.bookingnwt.analyticsservice.dto.PropertyStatisticsResponseDTO;
import com.bookingnwt.analyticsservice.exception.ResourceNotFoundException;
import com.bookingnwt.analyticsservice.mapper.PropertyStatisticsMapper;
import com.bookingnwt.analyticsservice.model.PropertyStatistics;
import com.bookingnwt.analyticsservice.repository.PropertyStatisticsRepository;
import com.bookingnwt.analyticsservice.service.impl.PropertyStatisticsServiceImpl;
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
class PropertyStatisticsServiceTest {

    @Mock
    private PropertyStatisticsRepository repository;

    @Mock
    private PropertyStatisticsMapper mapper;

    @InjectMocks
    private PropertyStatisticsServiceImpl service;

    private PropertyStatistics entity;
    private PropertyStatisticsRequestDTO requestDTO;
    private PropertyStatisticsResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        entity = new PropertyStatistics();
        entity.setId(1L);
        entity.setPropertyId(1L);
        entity.setHostId(2L);
        entity.setYear(2026);
        entity.setMonth(4);
        entity.setTotalReservations(7);
        entity.setTotalRevenue(new BigDecimal("1890.00"));
        entity.setAverageRating(new BigDecimal("4.82"));
        entity.setOccupancyRate(new BigDecimal("62.00"));
        entity.setViewCount(250);
        entity.setCancellationCount(1);
        entity.setCreatedAt(LocalDateTime.now());

        requestDTO = new PropertyStatisticsRequestDTO();
        requestDTO.setPropertyId(1L);
        requestDTO.setHostId(2L);
        requestDTO.setYear(2026);
        requestDTO.setMonth(4);
        requestDTO.setTotalReservations(7);
        requestDTO.setTotalRevenue(new BigDecimal("1890.00"));
        requestDTO.setAverageRating(new BigDecimal("4.82"));
        requestDTO.setOccupancyRate(new BigDecimal("62.00"));
        requestDTO.setViewCount(250);
        requestDTO.setCancellationCount(1);

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
        responseDTO.setCreatedAt(entity.getCreatedAt());
    }

    @Test
    void createStatistics_Success() {
        when(mapper.toEntity(requestDTO)).thenReturn(entity);
        when(repository.save(any(PropertyStatistics.class))).thenReturn(entity);
        when(mapper.toDTO(entity)).thenReturn(responseDTO);

        PropertyStatisticsResponseDTO result = service.createStatistics(requestDTO);

        assertNotNull(result);
        assertEquals(2026, result.getYear());
        assertEquals(4, result.getMonth());
        verify(repository, times(1)).save(any(PropertyStatistics.class));
    }

    @Test
    void getStatisticsById_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDTO(entity)).thenReturn(responseDTO);

        PropertyStatisticsResponseDTO result = service.getStatisticsById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getStatisticsById_NotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getStatisticsById(99L));
    }

    @Test
    void getAllStatistics_Success() {
        PropertyStatistics e2 = new PropertyStatistics();
        e2.setId(2L);
        PropertyStatisticsResponseDTO r2 = new PropertyStatisticsResponseDTO();
        r2.setId(2L);

        when(repository.findAll()).thenReturn(Arrays.asList(entity, e2));
        when(mapper.toDTO(entity)).thenReturn(responseDTO);
        when(mapper.toDTO(e2)).thenReturn(r2);

        List<PropertyStatisticsResponseDTO> result = service.getAllStatistics();

        assertEquals(2, result.size());
    }

    @Test
    void getStatisticsByPropertyId_Success() {
        when(repository.findByPropertyId(1L)).thenReturn(List.of(entity));
        when(mapper.toDTO(entity)).thenReturn(responseDTO);

        List<PropertyStatisticsResponseDTO> result = service.getStatisticsByPropertyId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getPropertyId());
    }

    @Test
    void getStatisticsByHostId_Success() {
        when(repository.findByHostId(2L)).thenReturn(List.of(entity));
        when(mapper.toDTO(entity)).thenReturn(responseDTO);

        List<PropertyStatisticsResponseDTO> result = service.getStatisticsByHostId(2L);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getHostId());
    }

    @Test
    void getStatisticsByHostIdAndPeriod_Success() {
        when(repository.findByHostIdAndYearAndMonth(2L, 2026, 4)).thenReturn(List.of(entity));
        when(mapper.toDTO(entity)).thenReturn(responseDTO);

        List<PropertyStatisticsResponseDTO> result = service.getStatisticsByHostIdAndPeriod(2L, 2026, 4);

        assertEquals(1, result.size());
    }

    @Test
    void deleteStatistics_Success() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        assertDoesNotThrow(() -> service.deleteStatistics(1L));
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void deleteStatistics_NotFound() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteStatistics(99L));
    }
}
