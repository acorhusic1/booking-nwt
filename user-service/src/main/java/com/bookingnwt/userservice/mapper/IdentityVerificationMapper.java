package com.bookingnwt.userservice.mapper;

import com.bookingnwt.userservice.dto.IdentityVerificationRequest;
import com.bookingnwt.userservice.dto.IdentityVerificationResponse;
import com.bookingnwt.userservice.model.IdentityVerification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IdentityVerificationMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "status", expression = "java(verification.getStatus().name())")
    IdentityVerificationResponse toResponse(IdentityVerification verification);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "verifiedAt", ignore = true)
    @Mapping(target = "verifiedBy", ignore = true)
    IdentityVerification toEntity(IdentityVerificationRequest request);
}
