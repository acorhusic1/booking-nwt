package com.bookingnwt.propertyservice.controller;

import com.bookingnwt.propertyservice.dto.SeasonalRuleRequest;
import com.bookingnwt.propertyservice.dto.SeasonalRuleResponse;
import com.bookingnwt.propertyservice.service.SeasonalRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties/{propertyId}/seasonal-rules")
@RequiredArgsConstructor
public class SeasonalRuleController {

    private final SeasonalRuleService seasonalRuleService;

    @GetMapping
    public ResponseEntity<List<SeasonalRuleResponse>> getRules(@PathVariable Long propertyId) {
        return ResponseEntity.ok(seasonalRuleService.getRulesByPropertyId(propertyId));
    }

    @PostMapping
    public ResponseEntity<SeasonalRuleResponse> addRule(@PathVariable Long propertyId,
                                                        @Valid @RequestBody SeasonalRuleRequest request) {
        SeasonalRuleResponse created = seasonalRuleService.addRule(propertyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long propertyId,
                                           @PathVariable Long ruleId) {
        seasonalRuleService.deleteRule(ruleId);
        return ResponseEntity.noContent().build();
    }
}
