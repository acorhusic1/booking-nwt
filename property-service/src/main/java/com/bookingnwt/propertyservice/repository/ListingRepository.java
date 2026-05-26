package com.bookingnwt.propertyservice.repository;

import com.bookingnwt.propertyservice.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findByPropertyId(Long propertyId);
    List<Listing> findByHostId(Long hostId);
}
