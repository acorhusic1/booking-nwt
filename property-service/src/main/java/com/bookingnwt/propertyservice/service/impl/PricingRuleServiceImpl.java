package com.bookingnwt.propertyservice.service.impl;

import com.bookingnwt.propertyservice.dto.PricingRuleRequest;
import com.bookingnwt.propertyservice.dto.PricingRuleResponse;
import com.bookingnwt.propertyservice.exception.ResourceNotFoundException;
import com.bookingnwt.propertyservice.mapper.PricingRuleMapper;
import com.bookingnwt.propertyservice.model.PricingRule;
import com.bookingnwt.propertyservice.model.Property;
import com.bookingnwt.propertyservice.repository.PricingRuleRepository;
import com.bookingnwt.propertyservice.repository.PropertyRepository;
import com.bookingnwt.propertyservice.service.PricingRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PricingRuleServiceImpl implements PricingRuleService {

    private final PricingRuleRepository pricingRuleRepository;
    private final PropertyRepository propertyRepository;
    private final PricingRuleMapper pricingRuleMapper;

    @Override
    @Transactional(readOnly = true)
    public PricingRuleResponse getPricingByPropertyId(Long propertyId) {
        PricingRule rule = pricingRuleRepository.findByPropertyId(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pravilo cijena za nekretninu " + propertyId + " nije pronađeno"));
        return pricingRuleMapper.toResponse(rule);
    }

    @Override
    public PricingRuleResponse updatePricing(Long propertyId, PricingRuleRequest request) {
        PricingRule existing = pricingRuleRepository.findByPropertyId(propertyId).orElse(null);
        if (existing != null) {
            pricingRuleMapper.updateEntity(request, existing);
            PricingRule updated = pricingRuleRepository.save(existing);
            return pricingRuleMapper.toResponse(updated);
        } else {
            Property property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Nekretnina sa ID " + propertyId + " nije pronađena"));
            PricingRule rule = pricingRuleMapper.toEntity(request);
            rule.setProperty(property);
            PricingRule saved = pricingRuleRepository.save(rule);
            return pricingRuleMapper.toResponse(saved);
        }
    }
}
