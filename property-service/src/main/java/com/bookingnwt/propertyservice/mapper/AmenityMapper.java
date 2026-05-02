package com.bookingnwt.propertyservice.mapper;

import com.bookingnwt.propertyservice.dto.AmenityRequest;
import com.bookingnwt.propertyservice.dto.AmenityResponse;
import com.bookingnwt.propertyservice.model.Amenity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AmenityMapper {

    @Mapping(target = "category", expression = "java(amenity.getCategory().name())")
    AmenityResponse toResponse(Amenity amenity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", expression = "java(com.bookingnwt.propertyservice.model.AmenityCategory.valueOf(request.getCategory()))")
    Amenity toEntity(AmenityRequest request);
}
