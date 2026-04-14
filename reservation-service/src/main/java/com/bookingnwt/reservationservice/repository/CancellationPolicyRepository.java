package com.bookingnwt.reservationservice.repository;

import com.bookingnwt.reservationservice.model.CancellationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CancellationPolicyRepository extends JpaRepository<CancellationPolicy, Long> {
    List<CancellationPolicy> findByPropertyId(Long propertyId);
}
