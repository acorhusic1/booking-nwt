package com.bookingnwt.analyticsservice.service;

import com.bookingnwt.analyticsservice.dto.PropertyStatisticsRequestDTO;
import com.bookingnwt.analyticsservice.dto.PropertyStatisticsResponseDTO;

import java.util.List;

public interface PropertyStatisticsService {

    PropertyStatisticsResponseDTO createStatistics(PropertyStatisticsRequestDTO dto);

    PropertyStatisticsResponseDTO getStatisticsById(Long id);

    List<PropertyStatisticsResponseDTO> getAllStatistics();

    List<PropertyStatisticsResponseDTO> getStatisticsByPropertyId(Long propertyId);

    List<PropertyStatisticsResponseDTO> getStatisticsByHostId(Long hostId);

    List<PropertyStatisticsResponseDTO> getStatisticsByHostIdAndPeriod(Long hostId, Integer year, Integer month);

    void deleteStatistics(Long id);
}
