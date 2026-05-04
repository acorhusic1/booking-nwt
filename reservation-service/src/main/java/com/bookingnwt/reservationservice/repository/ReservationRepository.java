package com.bookingnwt.reservationservice.repository;

import com.bookingnwt.reservationservice.model.Reservation;
import com.bookingnwt.reservationservice.model.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByGuestId(Long guestId);
    List<Reservation> findByPropertyId(Long propertyId);
    List<Reservation> findByHostId(Long hostId);
    List<Reservation> findByStatus(ReservationStatus status);

    @EntityGraph(attributePaths = {"cancellationPolicy", "promoCode", "problemReports"})
    Optional<Reservation> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"cancellationPolicy", "promoCode"})
    Page<Reservation> findByGuestId(Long guestId, Pageable pageable);

    @Query("""
            SELECT r FROM Reservation r
            WHERE r.guestId = :guestId
              AND r.checkIn >= :from
              AND r.checkOut <= :to
            ORDER BY r.checkIn ASC
            """)
    List<Reservation> findByGuestAndDateRange(@Param("guestId") Long guestId,
                                              @Param("from") LocalDate from,
                                              @Param("to") LocalDate to);

    @Query("""
            SELECT COALESCE(SUM(r.totalPrice), 0)
            FROM Reservation r
            WHERE r.hostId = :hostId
              AND r.status IN (com.bookingnwt.reservationservice.model.ReservationStatus.CONFIRMED,
                               com.bookingnwt.reservationservice.model.ReservationStatus.ACTIVE,
                               com.bookingnwt.reservationservice.model.ReservationStatus.COMPLETED)
            """)
    BigDecimal sumConfirmedRevenueByHost(@Param("hostId") Long hostId);

    @Query("""
            SELECT COUNT(r) > 0 FROM Reservation r
            WHERE r.propertyId = :propertyId
              AND r.status <> com.bookingnwt.reservationservice.model.ReservationStatus.CANCELLED
              AND r.checkIn < :checkOut
              AND r.checkOut > :checkIn
            """)
    boolean existsOverlap(@Param("propertyId") Long propertyId,
                          @Param("checkIn") LocalDate checkIn,
                          @Param("checkOut") LocalDate checkOut);
}
