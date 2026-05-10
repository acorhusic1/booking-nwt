package com.bookingnwt.systemevents.repository;

import com.bookingnwt.systemevents.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType);
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);

    /**
     * Task 4 - Pagination & Sorting (Pageable kontrolise page/size/sort).
     */
    Page<AuditLog> findAll(Pageable pageable);

    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    /**
     * Task 4 - Custom @Query (JPQL) sa optionalnim filterima:
     * userId, action, entityType, vremenski raspon. Svaki null param se ignorise.
     */
    @Query("""
            SELECT a FROM AuditLog a
            WHERE (:userId IS NULL OR a.userId = :userId)
              AND (:action IS NULL OR a.action = :action)
              AND (:entityType IS NULL OR a.entityType = :entityType)
              AND (:from IS NULL OR a.createdAt >= :from)
              AND (:to IS NULL OR a.createdAt <= :to)
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> filter(@Param("userId") Long userId,
                          @Param("action") String action,
                          @Param("entityType") String entityType,
                          @Param("from") LocalDateTime from,
                          @Param("to") LocalDateTime to,
                          Pageable pageable);

    /**
     * Task 4 - Native SQL upit za top N najcescih akcija (za izvjestaje).
     */
    @Query(value = """
            SELECT action, COUNT(*) AS count
            FROM audit_log
            WHERE created_at >= :from
            GROUP BY action
            ORDER BY count DESC
            """, nativeQuery = true)
    List<Object[]> topActionsSince(@Param("from") LocalDateTime from);
}
