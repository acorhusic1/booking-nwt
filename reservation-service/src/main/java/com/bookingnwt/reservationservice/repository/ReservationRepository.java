package com.bookingnwt.reservationservice.repository;

import com.bookingnwt.reservationservice.model.Reservation;
import com.bookingnwt.reservationservice.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByGuestId(Long guestId);
    List<Reservation> findByPropertyId(Long propertyId);
    List<Reservation> findByHostId(Long hostId);
    List<Reservation> findByStatus(ReservationStatus status);
}
