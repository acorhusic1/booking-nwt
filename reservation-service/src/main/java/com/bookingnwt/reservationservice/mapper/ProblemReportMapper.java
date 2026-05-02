package com.bookingnwt.reservationservice.mapper;

import com.bookingnwt.reservationservice.dto.ProblemReportRequestDTO;
import com.bookingnwt.reservationservice.dto.ProblemReportResponseDTO;
import com.bookingnwt.reservationservice.model.ProblemReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProblemReportMapper {

    @Mapping(source = "reservation.id", target = "reservationId")
    ProblemReportResponseDTO toResponseDTO(ProblemReport report);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservation", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "reportedAt", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    ProblemReport toEntity(ProblemReportRequestDTO dto);
}
