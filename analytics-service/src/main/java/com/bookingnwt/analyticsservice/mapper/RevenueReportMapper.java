package com.bookingnwt.analyticsservice.mapper;

import com.bookingnwt.analyticsservice.dto.RevenueReportRequestDTO;
import com.bookingnwt.analyticsservice.dto.RevenueReportResponseDTO;
import com.bookingnwt.analyticsservice.model.RevenueReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RevenueReportMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RevenueReport toEntity(RevenueReportRequestDTO dto);

    RevenueReportResponseDTO toDTO(RevenueReport entity);
}
