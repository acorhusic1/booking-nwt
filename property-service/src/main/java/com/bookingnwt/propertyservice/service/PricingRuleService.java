package com.bookingnwt.propertyservice.service;

import com.bookingnwt.propertyservice.dto.PricingRuleRequest;
import com.bookingnwt.propertyservice.dto.PricingRuleResponse;

public interface PricingRuleService {
    PricingRuleResponse getPricingByPropertyId(Long propertyId);
    PricingRuleResponse updatePricing(Long propertyId, PricingRuleRequest request);
}
