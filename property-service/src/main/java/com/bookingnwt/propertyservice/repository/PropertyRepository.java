package com.bookingnwt.propertyservice.repository;

import com.bookingnwt.propertyservice.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByHostId(Long hostId);
    List<Property> findByCityAndIsActiveTrue(String city);
}
