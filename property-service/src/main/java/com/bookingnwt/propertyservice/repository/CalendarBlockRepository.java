package com.bookingnwt.propertyservice.repository;

import com.bookingnwt.propertyservice.model.CalendarBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarBlockRepository extends JpaRepository<CalendarBlock, Long> {
    List<CalendarBlock> findByPropertyId(Long propertyId);
}
