package com.bookingnwt.userservice.mapper;

import com.bookingnwt.userservice.dto.UserPreferenceRequest;
import com.bookingnwt.userservice.dto.UserPreferenceResponse;
import com.bookingnwt.userservice.model.UserPreference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserPreferenceMapper {

    @Mapping(target = "userId", source = "user.id")
    UserPreferenceResponse toResponse(UserPreference preference);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserPreference toEntity(UserPreferenceRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UserPreferenceRequest request, @MappingTarget UserPreference preference);
}
