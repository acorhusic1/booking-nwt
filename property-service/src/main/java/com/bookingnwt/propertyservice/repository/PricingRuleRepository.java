package com.bookingnwt.propertyservice.repository;

import com.bookingnwt.propertyservice.model.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    Optional<PricingRule> findByPropertyId(Long propertyId);
}
