package com.bookingnwt.propertyservice.mapper;

import com.bookingnwt.propertyservice.dto.PropertyImageRequest;
import com.bookingnwt.propertyservice.dto.PropertyImageResponse;
import com.bookingnwt.propertyservice.model.PropertyImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PropertyImageMapper {

    @Mapping(target = "propertyId", source = "property.id")
    PropertyImageResponse toResponse(PropertyImage image);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    PropertyImage toEntity(PropertyImageRequest request);
}
