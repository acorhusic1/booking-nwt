package com.bookingnwt.analyticsservice.mapper;

import com.bookingnwt.analyticsservice.dto.PropertyStatisticsRequestDTO;
import com.bookingnwt.analyticsservice.dto.PropertyStatisticsResponseDTO;
import com.bookingnwt.analyticsservice.model.PropertyStatistics;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PropertyStatisticsMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PropertyStatistics toEntity(PropertyStatisticsRequestDTO dto);

    PropertyStatisticsResponseDTO toDTO(PropertyStatistics entity);
}
