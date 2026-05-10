package com.bookingnwt.notificationservice.service;

import com.bookingnwt.notificationservice.dto.NotificationBatchRequestDTO;
import com.bookingnwt.notificationservice.dto.NotificationPatchDTO;
import com.bookingnwt.notificationservice.dto.NotificationRequestDTO;
import com.bookingnwt.notificationservice.dto.NotificationResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationService {

    NotificationResponseDTO createNotification(NotificationRequestDTO dto);

    /**
     * Task 4 - Batch insert (saveAll + Hibernate JDBC batch).
     */
    List<NotificationResponseDTO> createBatch(NotificationBatchRequestDTO batch);

    NotificationResponseDTO getNotificationById(Long id);

    List<NotificationResponseDTO> getAllNotifications();

    /**
     * Task 4 - Pagination & Sorting na svim notifikacijama.
     */
    Page<NotificationResponseDTO> getAllNotifications(Pageable pageable);

    List<NotificationResponseDTO> getNotificationsByUserId(Long userId);

    Page<NotificationResponseDTO> getNotificationsByUserId(Long userId, Pageable pageable);

    /**
     * Task 4 - Custom JPQL query (filtriranje + paginacija).
     */
    Page<NotificationResponseDTO> search(Long userId, String type, Boolean isRead,
                                        LocalDateTime from, LocalDateTime to, Pageable pageable);

    long countUnreadByUserId(Long userId);

    NotificationResponseDTO markAsRead(Long id);

    /**
     * Task 4 - PATCH (parcijalno azuriranje).
     */
    NotificationResponseDTO patchNotification(Long id, NotificationPatchDTO patch);

    void deleteNotification(Long id);
}
