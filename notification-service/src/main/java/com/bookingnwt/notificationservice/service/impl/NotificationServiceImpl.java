package com.bookingnwt.notificationservice.service.impl;

import com.bookingnwt.notificationservice.dto.NotificationBatchRequestDTO;
import com.bookingnwt.notificationservice.dto.NotificationPatchDTO;
import com.bookingnwt.notificationservice.dto.NotificationRequestDTO;
import com.bookingnwt.notificationservice.dto.NotificationResponseDTO;
import com.bookingnwt.notificationservice.exception.ResourceNotFoundException;
import com.bookingnwt.notificationservice.mapper.NotificationMapper;
import com.bookingnwt.notificationservice.model.Notification;
import com.bookingnwt.notificationservice.repository.NotificationRepository;
import com.bookingnwt.notificationservice.service.AuditLogger;
import com.bookingnwt.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    /**
     * Task 5 - Sinhroni audit log poziv. Optional jer u testovima/legacy okruzenjima
     * AuditLogger moze biti odsutan; tada notifikacije rade isto, samo bez audit upisa.
     */
    @Autowired(required = false)
    private AuditLogger auditLogger;

    @Override
    @Transactional
    public NotificationResponseDTO createNotification(NotificationRequestDTO dto) {
        Notification notification = notificationMapper.toEntity(dto);
        notification.setIsRead(false);
        Notification saved = notificationRepository.save(notification);

        // Task 5 - sinhroni Feign poziv prema system-events-service.
        // Fallback je vec u AuditLogClientFallback; AuditLogger to dodatno apsorbuje.
        if (auditLogger != null) {
            auditLogger.log(saved.getUserId(), "CREATE", "NOTIFICATION", saved.getId(),
                    "Kreirana notifikacija tipa " + saved.getType() + ": " + saved.getTitle());
        }

        return notificationMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public List<NotificationResponseDTO> createBatch(NotificationBatchRequestDTO batch) {
        // Task 4 - Batch insert preko saveAll() (Hibernate JDBC batch_size aktivan).
        List<Notification> entities = batch.getNotifications().stream()
                .map(notificationMapper::toEntity)
                .peek(n -> n.setIsRead(false))
                .toList();
        List<Notification> saved = notificationRepository.saveAll(entities);

        if (auditLogger != null && !saved.isEmpty()) {
            auditLogger.log(saved.get(0).getUserId(), "BATCH_CREATE", "NOTIFICATION",
                    null, "Batch unos " + saved.size() + " notifikacija");
        }

        return saved.stream().map(notificationMapper::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponseDTO getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notifikacija sa ID " + id + " nije pronađena"));
        return notificationMapper.toDTO(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(notificationMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable).map(notificationMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(notificationMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getNotificationsByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable).map(notificationMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> search(Long userId, String type, Boolean isRead,
                                                LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return notificationRepository.search(userId, type, isRead, from, to, pageable)
                .map(notificationMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadByUserId(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public NotificationResponseDTO markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notifikacija sa ID " + id + " nije pronađena"));
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        return notificationMapper.toDTO(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public NotificationResponseDTO patchNotification(Long id, NotificationPatchDTO patch) {
        // Task 4 - PATCH (parcijalno azuriranje); samo polja koja klijent posalje se mijenjaju.
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notifikacija sa ID " + id + " nije pronađena"));

        if (patch.getTitle() != null)   notification.setTitle(patch.getTitle());
        if (patch.getContent() != null) notification.setContent(patch.getContent());
        if (patch.getType() != null)    notification.setType(patch.getType());
        if (patch.getIsRead() != null) {
            notification.setIsRead(patch.getIsRead());
            if (Boolean.TRUE.equals(patch.getIsRead()) && notification.getReadAt() == null) {
                notification.setReadAt(LocalDateTime.now());
            }
            if (Boolean.FALSE.equals(patch.getIsRead())) {
                notification.setReadAt(null);
            }
        }
        return notificationMapper.toDTO(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notifikacija sa ID " + id + " nije pronađena");
        }
        notificationRepository.deleteById(id);
    }
}
