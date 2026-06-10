package com.bookingnwt.propertyservice.repository;

import com.bookingnwt.propertyservice.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPropertyId(Long propertyId);
    List<Review> findByGuestId(Long guestId);
    // F7 — jedna recenzija po rezervaciji (uz unique constraint na koloni)
    boolean existsByReservationId(Long reservationId);
}
