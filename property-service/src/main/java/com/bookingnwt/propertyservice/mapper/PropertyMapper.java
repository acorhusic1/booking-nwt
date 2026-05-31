package com.bookingnwt.propertyservice.mapper;

import com.bookingnwt.propertyservice.dto.PropertyRequest;
import com.bookingnwt.propertyservice.dto.PropertyResponse;
import com.bookingnwt.propertyservice.model.Property;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PropertyMapper {

    @Mapping(target = "primaryImageUrl", expression = "java(getPrimaryImageUrl(property))")
    @Mapping(target = "basePrice", expression = "java(getBasePrice(property))")
    PropertyResponse toResponse(Property property);

    default String getPrimaryImageUrl(Property property) {
        if (property.getImages() == null || property.getImages().isEmpty()) {
            return null;
        }
        return property.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .map(img -> img.getUrl())
                .findFirst()
                .orElse(property.getImages().get(0).getUrl());
    }

    default java.math.BigDecimal getBasePrice(Property property) {
        return property.getPricingRule() != null ? property.getPricingRule().getBasePrice() : null;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    @Mapping(target = "pricingRule", ignore = true)
    @Mapping(target = "calendarBlocks", ignore = true)
    @Mapping(target = "seasonalRules", ignore = true)
    @Mapping(target = "available", constant = "true")
    @Mapping(target = "moderationStatus", ignore = true) // entity default = "PENDING"
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
    @Mapping(target = "moderationStatus", ignore = true) // host ne smije sam mijenjati status
    void updateEntity(PropertyRequest request, @MappingTarget Property property);
}
