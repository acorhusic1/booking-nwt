package com.bookingnwt.propertyservice.controller;

import com.bookingnwt.propertyservice.dto.PricingRuleRequest;
import com.bookingnwt.propertyservice.dto.PricingRuleResponse;
import com.bookingnwt.propertyservice.service.PricingRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/properties/{propertyId}/pricing")
@RequiredArgsConstructor
public class PricingRuleController {

    private final PricingRuleService pricingRuleService;

    @GetMapping
    public ResponseEntity<PricingRuleResponse> getPricing(@PathVariable Long propertyId) {
        return ResponseEntity.ok(pricingRuleService.getPricingByPropertyId(propertyId));
    }

    @PutMapping
    public ResponseEntity<PricingRuleResponse> updatePricing(@PathVariable Long propertyId,
                                                             @Valid @RequestBody PricingRuleRequest request) {
        return ResponseEntity.ok(pricingRuleService.updatePricing(propertyId, request));
    }
}
