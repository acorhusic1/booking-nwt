package com.bookingnwt.propertyservice.service.impl;

import com.bookingnwt.propertyservice.dto.SeasonalRuleRequest;
import com.bookingnwt.propertyservice.dto.SeasonalRuleResponse;
import com.bookingnwt.propertyservice.exception.ResourceNotFoundException;
import com.bookingnwt.propertyservice.mapper.SeasonalRuleMapper;
import com.bookingnwt.propertyservice.model.Property;
import com.bookingnwt.propertyservice.model.SeasonalRule;
import com.bookingnwt.propertyservice.repository.PropertyRepository;
import com.bookingnwt.propertyservice.repository.SeasonalRuleRepository;
import com.bookingnwt.propertyservice.service.SeasonalRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SeasonalRuleServiceImpl implements SeasonalRuleService {

    private final SeasonalRuleRepository seasonalRuleRepository;
    private final PropertyRepository propertyRepository;
    private final SeasonalRuleMapper seasonalRuleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SeasonalRuleResponse> getRulesByPropertyId(Long propertyId) {
        return seasonalRuleRepository.findByPropertyId(propertyId)
                .stream()
                .map(seasonalRuleMapper::toResponse)
                .toList();
    }

    @Override
    public SeasonalRuleResponse addRule(Long propertyId, SeasonalRuleRequest request) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Nekretnina sa ID " + propertyId + " nije pronađena"));
        SeasonalRule rule = seasonalRuleMapper.toEntity(request);
        rule.setProperty(property);
        SeasonalRule saved = seasonalRuleRepository.save(rule);
        return seasonalRuleMapper.toResponse(saved);
    }

    @Override
    public void deleteRule(Long ruleId) {
        if (!seasonalRuleRepository.existsById(ruleId)) {
            throw new ResourceNotFoundException("Sezonsko pravilo sa ID " + ruleId + " nije pronađeno");
        }
        seasonalRuleRepository.deleteById(ruleId);
    }
}
