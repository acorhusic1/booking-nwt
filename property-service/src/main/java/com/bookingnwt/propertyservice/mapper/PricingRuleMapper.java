package com.bookingnwt.propertyservice.mapper;

import com.bookingnwt.propertyservice.dto.PricingRuleRequest;
import com.bookingnwt.propertyservice.dto.PricingRuleResponse;
import com.bookingnwt.propertyservice.model.PricingRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PricingRuleMapper {

    @Mapping(target = "propertyId", source = "property.id")
    PricingRuleResponse toResponse(PricingRule rule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    PricingRule toEntity(PricingRuleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(PricingRuleRequest request, @MappingTarget PricingRule rule);
}
