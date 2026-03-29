package com.bookingnwt.propertyservice.repository;

import com.bookingnwt.propertyservice.model.SeasonalRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeasonalRuleRepository extends JpaRepository<SeasonalRule, Long> {
}
