package com.bookingnwt.reservationservice.mapper;

import com.bookingnwt.reservationservice.dto.CancellationPolicyRequestDTO;
import com.bookingnwt.reservationservice.dto.CancellationPolicyResponseDTO;
import com.bookingnwt.reservationservice.model.CancellationPolicy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CancellationPolicyMapper {

    CancellationPolicyResponseDTO toResponseDTO(CancellationPolicy policy);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    CancellationPolicy toEntity(CancellationPolicyRequestDTO dto);
}
