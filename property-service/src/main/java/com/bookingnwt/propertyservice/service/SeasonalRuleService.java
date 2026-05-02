package com.bookingnwt.propertyservice.service;

import com.bookingnwt.propertyservice.dto.SeasonalRuleRequest;
import com.bookingnwt.propertyservice.dto.SeasonalRuleResponse;

import java.util.List;

public interface SeasonalRuleService {
    List<SeasonalRuleResponse> getRulesByPropertyId(Long propertyId);
    SeasonalRuleResponse addRule(Long propertyId, SeasonalRuleRequest request);
    void deleteRule(Long ruleId);
}
