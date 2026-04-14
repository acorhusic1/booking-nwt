package com.bookingnwt.notificationservice.service.impl;

import com.bookingnwt.notificationservice.dto.NotificationRequestDTO;
import com.bookingnwt.notificationservice.dto.NotificationResponseDTO;
import com.bookingnwt.notificationservice.exception.ResourceNotFoundException;
import com.bookingnwt.notificationservice.mapper.NotificationMapper;
import com.bookingnwt.notificationservice.model.Notification;
import com.bookingnwt.notificationservice.repository.NotificationRepository;
import com.bookingnwt.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public NotificationResponseDTO createNotification(NotificationRequestDTO dto) {
        Notification notification = notificationMapper.toEntity(dto);
        notification.setIsRead(false);
        return notificationMapper.toDTO(notificationRepository.save(notification));
    }

    @Override
    public NotificationResponseDTO getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notifikacija sa ID " + id + " nije pronađena"));
        return notificationMapper.toDTO(notification);
    }

    @Override
    public List<NotificationResponseDTO> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponseDTO> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResponseDTO markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notifikacija sa ID " + id + " nije pronađena"));
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        return notificationMapper.toDTO(notificationRepository.save(notification));
    }

    @Override
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notifikacija sa ID " + id + " nije pronađena");
        }
        notificationRepository.deleteById(id);
    }
}
