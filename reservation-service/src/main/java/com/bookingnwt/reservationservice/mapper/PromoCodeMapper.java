package com.bookingnwt.reservationservice.mapper;

import com.bookingnwt.reservationservice.dto.PromoCodeRequestDTO;
import com.bookingnwt.reservationservice.dto.PromoCodeResponseDTO;
import com.bookingnwt.reservationservice.model.PromoCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PromoCodeMapper {

    PromoCodeResponseDTO toResponseDTO(PromoCode promoCode);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usageCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    PromoCode toEntity(PromoCodeRequestDTO dto);
}
