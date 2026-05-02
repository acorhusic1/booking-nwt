package com.bookingnwt.propertyservice.mapper;

import com.bookingnwt.propertyservice.dto.SeasonalRuleRequest;
import com.bookingnwt.propertyservice.dto.SeasonalRuleResponse;
import com.bookingnwt.propertyservice.model.SeasonalRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SeasonalRuleMapper {

    @Mapping(target = "propertyId", source = "property.id")
    SeasonalRuleResponse toResponse(SeasonalRule rule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    SeasonalRule toEntity(SeasonalRuleRequest request);
}
