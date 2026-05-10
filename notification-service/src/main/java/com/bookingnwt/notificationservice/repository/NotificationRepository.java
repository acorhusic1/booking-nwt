package com.bookingnwt.notificationservice.repository;

import com.bookingnwt.notificationservice.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Task 4 - Pagination & Sorting:
     * Vraca stranicu notifikacija za korisnika (Pageable kontrolise page/size/sort).
     */
    Page<Notification> findByUserId(Long userId, Pageable pageable);

    /**
     * Task 4 - Custom @Query (JPQL):
     * Filtriranje notifikacija po tipu i opcionalnom datumskom intervalu.
     * "from" i "to" mogu biti null (filter se ignorise) zbog COALESCE-like uslova.
     */
    @Query("""
            SELECT n FROM Notification n
            WHERE (:userId IS NULL OR n.userId = :userId)
              AND (:type IS NULL OR n.type = :type)
              AND (:isRead IS NULL OR n.isRead = :isRead)
              AND (:from IS NULL OR n.createdAt >= :from)
              AND (:to IS NULL OR n.createdAt <= :to)
            ORDER BY n.createdAt DESC
            """)
    Page<Notification> search(@Param("userId") Long userId,
                              @Param("type") String type,
                              @Param("isRead") Boolean isRead,
                              @Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to,
                              Pageable pageable);

    /**
     * Task 4 - Custom @Query (broj nepročitanih notifikacija po korisniku).
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);
}
