package com.bookingnwt.propertyservice.mapper;

import com.bookingnwt.propertyservice.dto.CalendarBlockRequest;
import com.bookingnwt.propertyservice.dto.CalendarBlockResponse;
import com.bookingnwt.propertyservice.model.CalendarBlock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CalendarBlockMapper {

    @Mapping(target = "propertyId", source = "property.id")
    CalendarBlockResponse toResponse(CalendarBlock block);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "property", ignore = true)
    CalendarBlock toEntity(CalendarBlockRequest request);
}
