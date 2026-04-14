package com.bookingnwt.systemevents.repository;

import com.bookingnwt.systemevents.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType);
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);
}
