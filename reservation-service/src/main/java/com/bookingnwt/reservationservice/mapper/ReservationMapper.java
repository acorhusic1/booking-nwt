package com.bookingnwt.reservationservice.mapper;

import com.bookingnwt.reservationservice.dto.ReservationRequestDTO;
import com.bookingnwt.reservationservice.dto.ReservationResponseDTO;
import com.bookingnwt.reservationservice.model.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(source = "cancellationPolicy.id", target = "cancellationPolicyId")
    @Mapping(source = "promoCode.id", target = "promoCodeId")
    ReservationResponseDTO toResponseDTO(Reservation reservation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "cancellationPolicy", ignore = true)
    @Mapping(target = "promoCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "problemReports", ignore = true)
    Reservation toEntity(ReservationRequestDTO dto);
}
