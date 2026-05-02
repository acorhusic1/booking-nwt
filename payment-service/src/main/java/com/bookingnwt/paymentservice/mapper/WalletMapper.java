package com.bookingnwt.paymentservice.mapper;

import com.bookingnwt.paymentservice.dto.WalletRequestDTO;
import com.bookingnwt.paymentservice.dto.WalletResponseDTO;
import com.bookingnwt.paymentservice.model.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    Wallet toEntity(WalletRequestDTO dto);

    WalletResponseDTO toDTO(Wallet wallet);
}
