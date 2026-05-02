package com.bookingnwt.paymentservice.mapper;

import com.bookingnwt.paymentservice.dto.WalletTransactionResponseDTO;
import com.bookingnwt.paymentservice.model.WalletTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletTransactionMapper {

    @Mapping(source = "wallet.id", target = "walletId")
    @Mapping(source = "payment.id", target = "paymentId")
    WalletTransactionResponseDTO toDTO(WalletTransaction transaction);
}
