package com.bookingnwt.notificationservice.service;

import com.bookingnwt.notificationservice.dto.NotificationRequestDTO;
import com.bookingnwt.notificationservice.dto.NotificationResponseDTO;

import java.util.List;

public interface NotificationService {
    NotificationResponseDTO createNotification(NotificationRequestDTO dto);
    NotificationResponseDTO getNotificationById(Long id);
    List<NotificationResponseDTO> getAllNotifications();
    List<NotificationResponseDTO> getNotificationsByUserId(Long userId);
    NotificationResponseDTO markAsRead(Long id);
    void deleteNotification(Long id);
}
