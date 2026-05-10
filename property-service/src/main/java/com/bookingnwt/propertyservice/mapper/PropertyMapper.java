package com.bookingnwt.propertyservice.mapper;

import com.bookingnwt.propertyservice.dto.PropertyRequest;
import com.bookingnwt.propertyservice.dto.PropertyResponse;
import com.bookingnwt.propertyservice.model.Property;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PropertyMapper {

    PropertyResponse toResponse(Property property);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    @Mapping(target = "pricingRule", ignore = true)
    @Mapping(target = "calendarBlocks", ignore = true)
    @Mapping(target = "seasonalRules", ignore = true)
    @Mapping(target = "available", constant = "true")
    Property toEntity(PropertyRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    @Mapping(target = "pricingRule", ignore = true)
    @Mapping(target = "calendarBlocks", ignore = true)
    @Mapping(target = "seasonalRules", ignore = true)
    @Mapping(target = "available", ignore = true)
    void updateEntity(PropertyRequest request, @MappingTarget Property property);
}
